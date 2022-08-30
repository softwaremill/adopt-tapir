import {useEffect, useState} from 'react';
import {Alert, Backdrop, Box, Button, CircularProgress, Snackbar, Typography} from '@mui/material';
import { FormProvider, useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { DevTool } from '@hookform/devtools';
import { saveAs } from 'file-saver';
import { Builder, JSONImplementation, ScalaVersion, StarterRequest } from 'api/starter';
import { useApiCall } from 'hooks/useApiCall';
import { isDevelopment } from 'consts/env';
import { FormTextField } from '../FormTextField';
import { FormSelect } from '../FormSelect';
import { FormRadioGroup } from '../FormRadioGroup';
import { useStyles } from './ConfigurationForm.styles';
import {
  BUILDER_OPTIONS,
  EFFECT_TYPE_OPTIONS,
  ENDPOINTS_OPTIONS,
  SCALA_VERSION_OPTIONS,
  starterValidationSchema,
} from './ConfigurationForm.consts';
import {
  getAvailableEffectImplementations,
  getEffectImplementationOptions,
  getJSONImplementationOptions,
  mapEffectTypeToJSONImplementation,
} from './ConfigurationForm.helpers';
import {useLocation, useNavigate} from "react-router-dom";

interface ConfigurationFormProps {
  isEmbedded?: boolean;
}

export const ConfigurationForm: React.FC<ConfigurationFormProps> = ({ isEmbedded = false }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { call, clearError, isLoading, errorMessage } = useApiCall();
  const { classes, cx } = useStyles({ isEmbedded });
  const form = useForm<StarterRequest>({
    mode: 'onBlur',
    resolver: yupResolver(starterValidationSchema),
    defaultValues: {
      addDocumentation: false,
      addMetrics: false,
      json: JSONImplementation.No,
      scalaVersion: ScalaVersion.Scala3,
      builder: Builder.Sbt,
    },
  });

  // TODO: improve type definitions in watch, as if they do not have default value they should be undefined
  const [effectType, effectImplementation, jsonImplementation, scalaVersion] = form.watch([
    'effect',
    'implementation',
    'json',
    'scalaVersion',
  ]);
  const isEffectImplementationSelectable = Boolean(effectType) && Boolean(scalaVersion);

  useEffect(() => {
    // state is passed from preview starter, after the click on 'Back' button.
    if (location.state !== null) {
      const request = location.state as StarterRequest;
      form.setValue('projectName', request.projectName);
      form.setValue('groupId', request.groupId);
      form.setValue('effect', request.effect);
      form.setValue('implementation', request.implementation);
      form.setValue('addDocumentation', request.addDocumentation);
      form.setValue('addMetrics', request.addMetrics);
      form.setValue('json', request.json);
      form.setValue('scalaVersion', request.scalaVersion);
      form.setValue('builder', request.builder);
    }
  }, [location])

  useEffect(() => {
    // NOTE: reset effect implementation field value upon effect type or scala version change
    if (
      effectType &&
      scalaVersion &&
      !getAvailableEffectImplementations(effectType, scalaVersion).includes(effectImplementation)
    ) {
      form.resetField('implementation');
    }
  }, [form, effectType, effectImplementation, scalaVersion]);

  useEffect(() => {
    // NOTE: reset json field value upon effect type change
    if (
      effectType &&
      jsonImplementation &&
      !mapEffectTypeToJSONImplementation(effectType).includes(jsonImplementation)
    ) {
      form.resetField('json');
    }
  }, [form, effectType, jsonImplementation]);

  const handleStarterRequest = async (formData: StarterRequest): Promise<void> => {
    const serverAddress = (process.env.REACT_APP_SERVER_ADDRESS == null) ? "https://adopt-tapir.softwaremill.com" : process.env.REACT_APP_SERVER_ADDRESS;
    const response = await fetch(`${serverAddress}/api/v1/starter.zip`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData),
    });

    if (!response.ok) {
      const json = await response.json();

      throw new Error(json.error || 'Something went wrong, please try again later.');
    }

    const blob = await response.blob();
    const filename = response.headers.get('Content-Disposition')?.split('filename=')[1].replaceAll('"', '');

    // download starter zip file
    saveAs(blob, filename ?? 'starter.zip');
  };

  const handleFormSubmit = (formData: StarterRequest): void => {
    call(() => handleStarterRequest(formData));
  };

  const handleFormReset = (): void => {
    // Clear the state passed from preview starter page.
    window.history.replaceState({}, document.title)

    form.reset();
  };

  const handleShowPreview = (): void => {
    form.trigger()
      .then(isValid => {
        if (isValid) {
          navigate("/preview-starter", {state: form.getValues()});
        }
      })
  }

  const handleCloseAlert = (): void => {
    clearError();
  };

  return (
    <Box>
      {!isEmbedded && (
        <Typography variant="h3" component="h3" fontWeight={300} gutterBottom>
          Generate tapir project
        </Typography>
      )}
      <FormProvider {...form}>
        <form
          className={classes.formContainer}
          data-testid="configuration-form"
          noValidate
          onSubmit={form.handleSubmit(handleFormSubmit)}
        >
          <FormTextField
            className={classes.formFirstRow}
            name="projectName"
            label="Project name"
            placeholder="projectname"
          />
          <FormTextField
            className={classes.formFirstRow}
            name="groupId"
            label="Group ID"
            placeholder="com.softwaremill"
          />

          <FormSelect
            className={classes.formSecondRow}
            name="effect"
            label="Effect type"
            options={EFFECT_TYPE_OPTIONS}
          />
          <FormRadioGroup
            className={classes.formSecondRow}
            name="scalaVersion"
            label="Scala version"
            options={SCALA_VERSION_OPTIONS}
          />

          <FormSelect
            className={classes.formThirdRow}
            name="implementation"
            label="Server implementation"
            disabled={!isEffectImplementationSelectable}
            options={isEffectImplementationSelectable ? getEffectImplementationOptions(effectType, scalaVersion) : []}
          />
          <FormRadioGroup className={classes.formThirdRow} name="builder" label="Builder" options={BUILDER_OPTIONS} />

          <FormRadioGroup
            className={classes.formFourthRow}
            name="addDocumentation"
            label="Expose endpoint documentation using Swagger UI"
            options={ENDPOINTS_OPTIONS}
          />
          <FormRadioGroup
            className={classes.formFourthRow}
            name="json"
            label="Add JSON endpoint using"
            options={getJSONImplementationOptions(effectType)}
          />

          <FormRadioGroup
            className={classes.formFifthRow}
            name="addMetrics"
            label="Add metrics endpoints"
            options={ENDPOINTS_OPTIONS}
          />

          <div className={cx(classes.actionsContainer, classes.formActionsRow)}>
            <Button variant="contained" color="secondary" size="medium" onClick={handleFormReset} disableElevation>
              Reset
            </Button>

            <Button
              className={classes.submitButton}
              onClick={handleShowPreview}
              variant="contained"
              color="primary"
              size="medium"
              type="button"
              disableElevation
            >
              Preview
            </Button>

            <Button
              className={classes.submitButton}
              variant="contained"
              color="primary"
              size="medium"
              type="submit"
              disableElevation
            >
              Generate .zip
            </Button>
          </div>
        </form>

        {isDevelopment && <DevTool control={form.control} />}
      </FormProvider>

      <Backdrop open={isLoading}>
        <CircularProgress />
      </Backdrop>
      <Snackbar
        open={Boolean(errorMessage)}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        autoHideDuration={5000}
        onClose={handleCloseAlert}
      >
        <Alert severity="error" variant="outlined">
          {errorMessage}
        </Alert>
      </Snackbar>
    </Box>
  );
};
