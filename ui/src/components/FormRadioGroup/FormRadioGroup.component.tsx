import { FormControl, FormLabel, FormControlLabel, RadioGroup, Radio } from '@mui/material';
import { Controller, useFormContext } from 'react-hook-form';

export type FormRadioOption = {
  label: string;
  value: string | boolean;
};

interface FormRadioGroupProps {
  className?: string;
  name: string;
  label: string;
  options: FormRadioOption[];
  defaultValue?: FormRadioOption['value'];
}

// TODO: helper text?

export const FormRadioGroup: React.FC<FormRadioGroupProps> = ({ className, name, label, options, defaultValue }) => {
  const { control } = useFormContext();

  return (
    <Controller
      name={name}
      control={control}
      defaultValue={defaultValue}
      render={({ field, fieldState }) => (
        <FormControl className={className} error={Boolean(fieldState.error)}>
          <FormLabel>{label}</FormLabel>
          <RadioGroup row {...field}>
            {options.map((option, index) => (
              <FormControlLabel key={index} value={option.value} label={option.label} control={<Radio />} />
            ))}
          </RadioGroup>
        </FormControl>
      )}
    />
  );
};
