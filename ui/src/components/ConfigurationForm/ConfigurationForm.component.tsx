import { SyntheticEvent, useContext, useEffect, useState } from 'react';
import { AlertColor, Box, Button, IconButton, Stack, Tooltip, Typography } from '@mui/material';
import ShareTwoToneIcon from '@mui/icons-material/ShareTwoTone';
import { FormProvider, useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { DevTool } from '@hookform/devtools';
import { Builder, doRequestStarter, JSONImplementation, ScalaVersion, StarterRequest } from 'api/starter';
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
import { useSearchParams, useNavigate } from 'react-router-dom';
import { ApiCallAddons } from '../ApiCallAddons';
import { ConfigurationDataContext, resetFormData, setFormData } from '../../contexts';
import { parse, stringifyUrl } from 'query-string';
import { CommonSnackbar } from '../CommonSnackbar';

interface ConfigurationFormProps {
  isEmbedded?: boolean;
}

interface SnackbarConfig {
  open: boolean;
  severity?: AlertColor;
  message?: String;
}

export const ConfigurationForm: React.FC<ConfigurationFormProps> = ({ isEmbedded = false }) => {
  const navigate = useNavigate();
  const [{ formData }, contextDispatch] = useContext(ConfigurationDataContext);
  const { call, clearError, isLoading, errorMessage } = useApiCall();
  const { classes, cx } = useStyles({ isEmbedded });

  const [searchParams, setSearchParams] = useSearchParams();
  useEffect(() => applySharedConfiguration(searchParams));

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
    if (formData !== undefined) {
      form.setValue('projectName', formData.projectName);
      form.setValue('groupId', formData.groupId);
      form.setValue('effect', formData.effect);
      form.setValue('implementation', formData.implementation);
      form.setValue('addDocumentation', formData.addDocumentation);
      form.setValue('addMetrics', formData.addMetrics);
      form.setValue('json', formData.json);
      form.setValue('scalaVersion', formData.scalaVersion);
      form.setValue('builder', formData.builder);
    }
  }, [formData, form]);

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

  const handleFormSubmit = (formData: StarterRequest): void => {
    call(() => doRequestStarter(formData));
  };

  const handleFormReset = (): void => {
    contextDispatch(resetFormData());
    form.reset();
  };

  const handleShowPreview = (): void => {
    // 'trigger()' triggers the validation.
    form.trigger().then(isValid => {
      if (isValid) {
        const casted = form.getValues() as StarterRequest;
        // Conversion of bools is done by hand, because casting writes booleans as strings.
        const formData: StarterRequest = {
          ...casted,
          addDocumentation: 'true' === casted.addDocumentation.toString(),
          addMetrics: 'true' === casted.addMetrics.toString(),
        };
        contextDispatch(setFormData(formData));
        navigate('/preview-starter');
      }
    });
  };

  const [snackbar, setSnackbar] = useState<SnackbarConfig>({
    open: false,
    severity: 'info',
  });
  const handleSnackClose = (event?: SyntheticEvent | Event, reason?: string) => {
    if (reason === 'clickaway') {
      return;
    }
    setSnackbar({ ...snackbar, open: false });
  };

  const handleShareConfiguration = async () => {
    const casted = form.getValues() as StarterRequest;
    const urlToShare = stringifyUrl(
      { url: window.location.href, query: { ...casted } },
      { skipNull: true, skipEmptyString: true }
    );
    await navigator.clipboard.writeText(urlToShare);
    setSnackbar({ open: true, severity: 'info', message: 'Link to configuration copied to clipboard.' });
  };

  const applySharedConfiguration = (searchParams: URLSearchParams) => {
    if (searchParams.toString() !== '') {
      const qp = parse(searchParams.toString(), { parseBooleans: true });
      starterValidationSchema
        .isValid(qp)
        .then(isValid => {
          if (isValid) {
            const queryParams = qp as StarterRequest;
            let key: keyof StarterRequest;
            for (key in queryParams) {
              form.setValue(key, queryParams[key]);
            }
            setSnackbar({
              open: true,
              severity: 'info',
              message: 'Linked configuration was applied.',
            });
            //trigger form validity so that `share` button is enabled upon the values application
            form.trigger().then(ignore => {});
          } else {
            setSnackbar({
              open: true,
              severity: 'warning',
              message: 'Linked configuration is not valid therefore it was not applied.',
            });
          }
        })
        .catch(_ => {
          setSnackbar({
            open: true,
            severity: 'warning',
            message: 'Validation of linked configuration failed therefore it was not applied.',
          });
        });
      setSearchParams({});
    }
  };

  return (
    <Box>
      {!isEmbedded && (
        <Stack direction="row" alignItems="baseline" spacing={0.25}>
          <Typography variant="h3" component="h3" fontWeight={300} gutterBottom>
            Generate tapir project
          </Typography>
          <Tooltip title="Share configuration" arrow>
            <IconButton
              color="secondary"
              aria-label="share configuration"
              onClick={handleShareConfiguration}
              disabled={!form.formState.isValid}
            >
              <ShareTwoToneIcon />
            </IconButton>
          </Tooltip>
        </Stack>
      )}

      <CommonSnackbar
        duration={2500}
        message={snackbar.message}
        onClose={handleSnackClose}
        open={snackbar.open}
        severity={snackbar.severity}
      />

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
          <FormRadioGroup
            className={classes.formThirdRow}
            name="builder"
            label="Build tool"
            options={BUILDER_OPTIONS}
          />

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

            {!isEmbedded && (
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
            )}

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

      <ApiCallAddons isLoading={isLoading} clearError={clearError} errorMessage={errorMessage} />
    </Box>
  );
};
