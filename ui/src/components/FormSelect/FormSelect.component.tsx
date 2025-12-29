import { TextField, MenuItem } from '@mui/material';
import { Controller, useFormContext } from 'react-hook-form';

export type FormSelectOption<T extends string = string> = {
  label: string;
  value: T;
};

interface FormSelectProps {
  className?: string;
  name: string;
  label: string;
  options: FormSelectOption[];
  disabled?: boolean;
  defaultValue?: FormSelectOption['value'];
}

export const FormSelect: React.FC<FormSelectProps> = ({
  className,
  name,
  label,
  options,
  disabled,
  defaultValue = '',
}) => {
  const { control } = useFormContext();

  return (
    <Controller
      name={name}
      control={control}
      defaultValue={defaultValue}
      render={({ field, fieldState }) => (
        <TextField
          {...field}
          className={className}
          variant="outlined"
          size="small"
          margin="normal"
          color="secondary"
          label={label}
          disabled={disabled}
          error={Boolean(fieldState.error)}
          helperText={fieldState.error?.message}
          select
          fullWidth
          required
        >
          {options.map(option => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </TextField>
      )}
    />
  );
};
