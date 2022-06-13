import { useEffect, useState } from 'react';
import { Box, Button, Typography, CircularProgress, Backdrop, Snackbar, Alert } from '@mui/material';
import { useForm, FormProvider } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { StarterRequest } from 'api/starter';
import { FormTextField } from '../FormTextField';
import { FormSelect } from '../FormSelect';
import { FormRadioGroup } from '../FormRadioGroup';
import { useStyles } from './ConfigurationForm.styles';
import {
  starterValidationSchema,
  TAPIR_VERSION_OPTIONS,
  EFFECT_TYPE_OPTIONS,
  ENDPOINTS_OPTIONS,
} from './ConfigurationForm.consts';
import { mapEffectTypeToEffectImplementation, getEffectImplementationOptions } from './ConfigurationForm.helpers';

export const ConfigurationForm: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const { classes, cx } = useStyles();
  const form = useForm<StarterRequest>({
    mode: 'onBlur',
    resolver: yupResolver(starterValidationSchema),
  });

  const effectType = form.watch('effect');
  const effectImplementation = form.watch('implementation');
  const isEffectTypeSelected = Boolean(effectType);

  useEffect(() => {
    if (
      effectType &&
      effectImplementation &&
      !mapEffectTypeToEffectImplementation(effectType).includes(effectImplementation)
    ) {
      form.resetField('implementation');
    }
  }, [effectType, effectImplementation, form]);

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

      if (response.ok) {
        const blob = await response.blob();
        const file = URL.createObjectURL(blob);

        // download starter zip file
        window.location.assign(file);
      } else {
        const json = await response.json();

        throw new Error(json.error || 'Something went wrong, please try again later.');
      }
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
      <Typography variant="h3" component="h3" fontWeight={300} gutterBottom>
        Generate tapir project
      </Typography>
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

          <FormSelect
            className={classes.formVersionsRow}
            name="tapirVersion"
            label="Tapir version"
            options={TAPIR_VERSION_OPTIONS}
            defaultValue={TAPIR_VERSION_OPTIONS[0].value}
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
            disabled={!isEffectTypeSelected}
            options={isEffectTypeSelected ? getEffectImplementationOptions(effectType) : []}
          />

          <FormRadioGroup
            className={classes.formEndpointsRow}
            name="addDocumentation"
            label="Expose endpoint documentation using Swagger UI"
            options={ENDPOINTS_OPTIONS}
            defaultValue={false}
          />

          <div className={cx(classes.actionsContainer, classes.formActionsRow)}>
            <Button variant="contained" color="secondary" size="medium" onClick={handleFormReset} disableElevation>
              Reset
            </Button>

            <Button variant="contained" color="primary" size="medium" type="submit" disableElevation>
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
