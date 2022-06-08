import { useState } from 'react';
import { Box, Button, Typography, CircularProgress, Backdrop } from '@mui/material';
import { useForm, FormProvider } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { StarterRequest } from 'api/starter';
import { FormTextField } from '../FormTextField';
import { FormSelect } from '../FormSelect';
import { FormRadioGroup } from '../FormRadioGroup';
import { useStyles } from './ConfigurationForm.styles';
import {
  configurationSchema,
  TAPIR_VERSION_OPTIONS,
  EFFECT_TYPE_OPTIONS,
  EFFECT_IMPLEMENTATION_OPTIONS,
  ENDPOINTS_OPTIONS,
  JSON_INPUT_OUTPUT_OPTIONS,
} from './ConfigurationForm.consts';

// TODO:
// error handling
// make effect implementation options to be selectable based on effect type chosen

export const ConfigurationForm: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const { classes, cx } = useStyles();
  const form = useForm<StarterRequest>({ mode: 'onBlur', resolver: yupResolver(configurationSchema) });

  const handleFormSubmit = async (formData: StarterRequest) => {
    try {
      setIsLoading(true);

      const response = await fetch('https://adopt-tapir.softwaremill.com/api/v1/starter.zip', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });
      const blob = await response.blob();
      const file = URL.createObjectURL(blob);

      // download starter zip file
      window.location.assign(file);
    } catch (error) {
      // catch me if you can
    } finally {
      setIsLoading(false);
    }
  };

  const handleFormReset = () => {
    form.reset();
  };

  return (
    <Box>
      <Typography variant="h3" component="h3" fontWeight={300} gutterBottom>
        Generate tAPIr starter configuration
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
          />
          {/* <FormSelect name="scalaVersion" label="Scala version" options={[]} /> */}

          <FormSelect
            className={classes.formEffectsRow}
            name="effect"
            label="Effect type"
            options={EFFECT_TYPE_OPTIONS}
          />
          <FormSelect
            className={classes.formEffectsRow}
            name="implementation"
            label="Effect implementation"
            options={EFFECT_IMPLEMENTATION_OPTIONS}
          />

          <FormRadioGroup
            className={classes.formEndpointsRow}
            name="addDocumentation"
            label="Documentation endpoint"
            options={ENDPOINTS_OPTIONS}
            defaultValue={false}
          />
          {/* <FormRadioGroup
              name="addMetrics"
              label="Metrics endpoint"
              options={ENDPOINTS_OPTIONS}
              defaultValue={false}
            /> */}
          {/* <FormRadioGroup
              name="jsonInputOutput"
              label="JSON input/output"
              options={JSON_INPUT_OUTPUT_OPTIONS}
              defaultValue={false}
            /> */}

          <div className={cx(classes.actionsContainer, classes.formActionsRow)}>
            <Button variant="contained" color="secondary" size="medium" disableElevation onClick={handleFormReset}>
              Reset
            </Button>

            <Button variant="contained" color="primary" size="medium" disableElevation type="submit">
              Generate .zip
            </Button>
          </div>
        </form>
      </FormProvider>
      <Backdrop open={isLoading}>
        <CircularProgress />
      </Backdrop>
    </Box>
  );
};
