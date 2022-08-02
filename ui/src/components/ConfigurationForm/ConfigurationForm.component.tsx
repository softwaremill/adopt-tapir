import { useEffect, useState } from 'react';
import { Alert, Backdrop, Box, Button, CircularProgress, Snackbar, Typography } from '@mui/material';
import { FormProvider, useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { saveAs } from 'file-saver';
import { JSONImplementation, ScalaVersion, StarterRequest } from 'api/starter';
import { FormTextField } from '../FormTextField';
import { FormSelect } from '../FormSelect';
import { FormRadioGroup } from '../FormRadioGroup';
import { useStyles } from './ConfigurationForm.styles';
import {
  EFFECT_TYPE_OPTIONS,
  ENDPOINTS_OPTIONS,
  SCALA_VERSION_OPTIONS,
  starterValidationSchema,
} from './ConfigurationForm.consts';
import {
  forbiddenScala3EffectImplementations,
  getEffectImplementationOptions,
  getJSONImplementationOptions,
  isAddMetricsSupported,
  mapEffectTypeToEffectImplementation,
  mapEffectTypeToJSONImplementation,
} from './ConfigurationForm.helpers';

interface ConfigurationFormProps {
  isEmbedded?: boolean;
}

export const ConfigurationForm: React.FC<ConfigurationFormProps> = ({ isEmbedded = false }) => {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const { classes, cx } = useStyles({ isEmbedded });
  const form = useForm<StarterRequest>({
    mode: 'onBlur',
    resolver: yupResolver(starterValidationSchema),
    defaultValues: {
      addDocumentation: false,
      addMetrics: false,
      json: JSONImplementation.No,
    },
  });

  const [effectType, effectImplementation, jsonImplementation, scalaVer] = form.watch([
    'effect',
    'implementation',
    'json',
    'scalaVersion',
  ]);
  const isEffectImplementationSelectable = Boolean(effectType) && Boolean(scalaVer);

  useEffect(() => {
    // NOTE: reset effect implementation field value upon scala version change
    if (
      scalaVer &&
      scalaVer === ScalaVersion.Scala3 &&
      effectImplementation &&
      forbiddenScala3EffectImplementations.includes(effectImplementation)
    ) {
      form.resetField('implementation');
    }

    // NOTE: reset effect implementation field value upon effect type change
    if (
      effectType &&
      effectImplementation &&
      !mapEffectTypeToEffectImplementation(effectType).includes(effectImplementation)
    ) {
      form.resetField('implementation');
    }

    // NOTE: reset json field value upon effect type change
    if (
      effectType &&
      jsonImplementation &&
      !mapEffectTypeToJSONImplementation(effectType).includes(jsonImplementation)
    ) {
      form.resetField('json');
    }

    // NOTE: reset addMetrics field value if metrics are not supported
    if (!isAddMetricsSupported(effectImplementation)) {
      form.resetField('addMetrics');
    }
  }, [effectType, effectImplementation, jsonImplementation, form]);

  const handleFormSubmit = async (formData: StarterRequest): Promise<void> => {
    try {
      setIsLoading(true);

      const response = await fetch('https://adopt-tapir.softwaremill.com/api/v1/starter.zip', {
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
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(error as string);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleFormReset = (): void => {
    form.reset();
  };

  const handleCloseAlert = (): void => {
    setErrorMessage('');
  };

  return (
    <Box>
      {!isEmbedded && (
        <Typography variant="h3" component="h3" fontWeight={300} gutterBottom>
          Generate tapir project
        </Typography>
      )}
      <FormProvider {...form}>
        <form className={classes.formContainer} noValidate onSubmit={form.handleSubmit(handleFormSubmit)}>
          <FormTextField
            className={classes.formMetadataRow}
            name="projectName"
            label="Project name"
            placeholder="projectname"
          />
          <FormTextField
            className={classes.formMetadataRow}
            name="groupId"
            label="Group ID"
            placeholder="com.softwaremill"
          />
          <FormRadioGroup
            className={classes.formVersionsRow}
            name="scalaVersion"
            label="Scala version"
            options={SCALA_VERSION_OPTIONS}
          />
          <FormSelect
            className={classes.formEffectsRow}
            name="effect"
            label="Effect type"
            options={EFFECT_TYPE_OPTIONS}
          />
          <FormSelect
            className={classes.formEffectsRow}
            name="implementation"
            label="Server implementation"
            disabled={!isEffectImplementationSelectable}
            options={isEffectImplementationSelectable ? getEffectImplementationOptions(effectType, scalaVer) : []}
          />

          <FormRadioGroup
            className={classes.formEndpointsRow}
            name="addDocumentation"
            label="Expose endpoint documentation using Swagger UI"
            options={ENDPOINTS_OPTIONS}
          />

          <FormRadioGroup
            className={classes.formEndpointsRow}
            name="json"
            label="Add JSON endpoint using"
            options={getJSONImplementationOptions(effectType)}
          />

          <FormRadioGroup
            className={classes.formEndpoints2ndRow}
            name="addMetrics"
            label="Add metrics endpoints"
            disabled={!isAddMetricsSupported(effectImplementation)}
            options={ENDPOINTS_OPTIONS}
          />

          <div className={cx(classes.actionsContainer, classes.formActionsRow)}>
            <Button variant="contained" color="secondary" size="medium" onClick={handleFormReset} disableElevation>
              Reset
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
