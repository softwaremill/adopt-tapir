import { SyntheticEvent, useCallback, useContext, useEffect, useState } from 'react';
import { Box, Button, IconButton, Stack, Tooltip, Typography } from '@mui/material';
import ShareTwoToneIcon from '@mui/icons-material/ShareTwoTone';
import { FormProvider, useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { DevTool } from '@hookform/devtools';
import {
  Builder,
  doRequestStarter,
  JSONImplementation,
  ScalaVersion,
  serverAddress,
  StarterRequest,
} from 'api/starter';
import { useApiCall } from 'hooks/useApiCall';
import { isDevelopment } from 'consts/env';
import { FormTextField } from '../FormTextField';
import { FormSelect } from '../FormSelect';
import { FormRadioGroup } from '../FormRadioGroup';
import { useStyles } from './ConfigurationForm.styles';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';

import {
  BUILDER_OPTIONS,
  ENDPOINTS_OPTIONS,
  SCALA_VERSION_OPTIONS,
  starterValidationSchema,
} from './ConfigurationForm.consts';
import {
  getAvailableEffectImplementations,
  getEffectImplementationOptions,
  getStackTypeOptions,
  getJSONImplementationOptions,
  mapStackTypeToJSONImplementation,
  mapScalaVersionToStackType,
  mapScalaVersionToJSONImplementation,
} from './ConfigurationForm.helpers';
import { useNavigate } from 'react-router-dom';
import { ApiCallAddons } from '../ApiCallAddons';
import { ConfigurationDataContext, resetFormData, setFormData } from '../../contexts';
import { stringifyUrl } from 'query-string';
import { CommonSnackbar, SnackbarConfig } from '../CommonSnackbar';
import { useInitialData } from '../../hooks/useInitialData';
import { useSharedConfig } from '../../hooks/useSharedConfig';

interface ConfigurationFormProps {
  isEmbedded?: boolean;
}

export const ConfigurationForm: React.FC<ConfigurationFormProps> = ({ isEmbedded = false }) => {
  const navigate = useNavigate();
  const [{ formData }, contextDispatch] = useContext(ConfigurationDataContext);
  const { call, clearError, isLoading, errorMessage } = useApiCall();
  const { classes, cx } = useStyles({ isEmbedded });
  const initialData = useInitialData();
  const [initialized, setInitialized] = useState(false);

  const [sharedRequest, snackbarConfig, ready, preview] = useSharedConfig();

  const form = useForm<StarterRequest>({
    mode: 'onBlur',
    resolver: yupResolver(starterValidationSchema),
    defaultValues: {
      addDocumentation: false,
      addMetrics: false,
      projectName: '',
      groupId: '',
      json: JSONImplementation.No,
      scalaVersion: ScalaVersion.Scala3,
      builder: Builder.Sbt,
    },
  });

  const handleShowPreview = useCallback(() => {
    // 'trigger()' triggers the validation.
    form.trigger().then(isValid => {
      if (isValid) {
        const casted = form.getValues() as StarterRequest;
        // Conversion of bools is done by hand, because casting writes booleans as strings.
        const formData: StarterRequest = {
          ...casted,
          addDocumentation: JSON.parse(casted.addDocumentation.toString()),
          addMetrics: JSON.parse(casted.addMetrics.toString()),
        };
        contextDispatch(setFormData(formData));
        navigate('/preview-starter');
      }
    });
  }, [form, contextDispatch, navigate]);

  useEffect(() => {
    if (!ready || initialized) {
      return;
    }

    // We either load data from the link, from the preview or load initial data.
    // We don't set initial data as defaultValues:
    // - so reset button works correctly,
    // - so it is not loaded first, and replaced with sharedRequest or preview data, as it is visible to the user.
    const data = sharedRequest || formData || initialData;
    let key: keyof StarterRequest;
    for (key in data) {
      form.setValue(key, data[key]);
    }
    if (snackbarConfig !== undefined) {
      setSnackbar(snackbarConfig);
    }
    // Share config button works after validation, so we trigger it.
    form.trigger().then(_ => null);
    setInitialized(true);
    if (preview) {
      handleShowPreview();
    }
  }, [ready, initialized, sharedRequest, formData, initialData, snackbarConfig, form, preview, handleShowPreview]);

  // TODO: improve type definitions in watch, as if they do not have default value they should be undefined
  const [stackType, effectImplementation, jsonImplementation, scalaVersion] = form.watch([
    'stack',
    'implementation',
    'json',
    'scalaVersion',
  ]);
  const isEffectImplementationSelectable = Boolean(stackType) && Boolean(scalaVersion);

  useEffect(() => {
    // NOTE: reset stack type field value upon scala version change
    if (stackType && scalaVersion && !mapScalaVersionToStackType(scalaVersion).includes(stackType)) {
      form.resetField('stack');
    }
  }, [form, stackType, effectImplementation, scalaVersion]);

  useEffect(() => {
    // NOTE: reset effect implementation field value upon effect type or scala version change
    if (stackType && !getAvailableEffectImplementations(stackType).includes(effectImplementation)) {
      let availableEffectImplementations = getAvailableEffectImplementations(stackType);
      if (availableEffectImplementations.length > 0) {
        form.setValue('implementation', availableEffectImplementations[0]);
      } else {
        form.resetField('implementation');
      }
    }
  }, [form, stackType, effectImplementation, scalaVersion]);

  useEffect(() => {
    // NOTE: reset json field value upon effect type and scala version change
    if (
      stackType &&
      scalaVersion &&
      jsonImplementation &&
      !mapStackTypeToJSONImplementation(stackType).includes(jsonImplementation) &&
      !mapScalaVersionToJSONImplementation(scalaVersion).includes(jsonImplementation)
    ) {
      form.resetField('json');
    }
  }, [form, stackType, scalaVersion, jsonImplementation]);

  const handleFormSubmit = (formData: StarterRequest): void => {
    call(() => doRequestStarter(formData));
  };

  const handleFormReset = (): void => {
    contextDispatch(resetFormData());
    form.reset();
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

  const handleShowPreviewInNewTab = () => {
    // 'trigger()' triggers the validation.
    form.trigger().then(isValid => {
      if (isValid) {
        const casted = form.getValues() as StarterRequest;
        const urlToShare = stringifyUrl(
          { url: serverAddress, query: { ...casted, preview: 'true' } },
          { skipNull: true, skipEmptyString: true }
        );
        window.open(urlToShare, '_blank');
      }
    });
  };

  return (
    <Box>
      {!isEmbedded && (
        <Stack spacing={0.25} className={classes.headingWrapper}>
          <Typography variant="h3" component="h3" fontWeight={300}>
            Generate tapir project
          </Typography>
          <Tooltip title="Share configuration" arrow>
            <Box>
              <IconButton
                color="secondary"
                aria-label="share configuration"
                onClick={handleShareConfiguration}
                disabled={!form.formState.isValid}
              >
                <ShareTwoToneIcon />
              </IconButton>
            </Box>
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
          <fieldset className={classes.groupedInputs}>
            <legend className={classes.groupLegend}>Metadata</legend>
            <FormTextField name="projectName" label="Project name" placeholder="projectname" selectOnClick={true} />
            <FormTextField
              name="groupId"
              label="Group ID"
              placeholder="com.softwaremill"
              defaultValue="com.softwaremill"
              selectOnClick={true}
            />
          </fieldset>

          <fieldset className={classes.groupedInputs}>
            <legend className={classes.groupLegend}>Server</legend>
            <FormSelect name="stack" label="Stack" options={getStackTypeOptions(scalaVersion)} />
            <div className={classes.inputWithAddon}>
              <FormSelect
                name="implementation"
                label="Server implementation"
                disabled={!isEffectImplementationSelectable}
                options={isEffectImplementationSelectable ? getEffectImplementationOptions(stackType) : []}
              />
              <Tooltip
                title="Available options depend on the values of the 'Stack' and 'Scala version' inputs."
                className={classes.serverTooltip}
                enterDelay={50}
              >
                <Box>
                  <InfoOutlinedIcon />
                </Box>
              </Tooltip>
            </div>
          </fieldset>

          <fieldset className={classes.groupedInputs}>
            <legend className={classes.groupLegend}>Build</legend>
            <FormRadioGroup name="scalaVersion" label="Scala version" options={SCALA_VERSION_OPTIONS} />
            <FormRadioGroup name="builder" label="Build tool" options={BUILDER_OPTIONS} />
          </fieldset>

          <fieldset className={classes.groupedInputs}>
            <legend className={classes.groupLegend}>Options</legend>
            <FormRadioGroup
              name="json"
              label="Add JSON endpoint using"
              options={getJSONImplementationOptions(scalaVersion, stackType)}
            />
            <FormRadioGroup
              name="addDocumentation"
              label="Expose endpoint documentation using Swagger UI"
              options={ENDPOINTS_OPTIONS}
            />
            <FormRadioGroup name="addMetrics" label="Add metrics endpoints" options={ENDPOINTS_OPTIONS} />
          </fieldset>

          <div className={cx(classes.actionsContainer, classes.formActionsRow)}>
            <Button variant="contained" color="secondary" size="medium" onClick={handleFormReset} disableElevation>
              Reset
            </Button>

            <Button
              className={classes.submitButton}
              onClick={isEmbedded ? handleShowPreviewInNewTab : handleShowPreview}
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

      <ApiCallAddons isLoading={isLoading} clearError={clearError} errorMessage={errorMessage} />
    </Box>
  );
};
