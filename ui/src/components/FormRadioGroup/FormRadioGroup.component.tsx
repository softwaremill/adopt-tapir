import { FormControl, FormLabel, FormControlLabel, RadioGroup, Radio } from '@mui/material';
import { Controller, useFormContext } from 'react-hook-form';

export type FormRadioOption<T extends string | boolean = string | boolean> = {
  label: string;
  value: T;
};

interface FormRadioGroupProps {
  className?: string;
  name: string;
  label: string;
  options: FormRadioOption[];
  disabled?: boolean;
  defaultValue?: FormRadioOption['value'];
}

// NOTE: if radio group would need to show some error we would have to add a FormHelperText component here
export const FormRadioGroup: React.FC<FormRadioGroupProps> = ({
  className,
  name,
  label,
  options,
  disabled,
  defaultValue,
}) => {
  const { control } = useFormContext();

  return (
    <Controller
      name={name}
      control={control}
      defaultValue={defaultValue}
      render={({ field, fieldState }) => (
        <FormControl
          className={className}
          error={Boolean(fieldState.error)}
          color="secondary"
          margin="normal"
          disabled={disabled}
        >
          <FormLabel id={`form-control-label-${field.name}`}>{label}</FormLabel>
          <RadioGroup aria-labelledby={`form-control-label-${field.name}`} row {...field}>
            {options.map((option, index) => (
              <FormControlLabel key={index} value={option.value} label={option.label} control={<Radio />} />
            ))}
          </RadioGroup>
        </FormControl>
      )}
    />
  );
};
