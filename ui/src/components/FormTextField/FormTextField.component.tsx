import { TextField } from '@mui/material';
import { Controller, useFormContext } from 'react-hook-form';

interface FormTextFieldProps {
  className?: string;
  name: string;
  label: string;
  placeholder?: string;
  defaultValue?: string;
}

export const FormTextField: React.FC<FormTextFieldProps> = ({
  className,
  name,
  label,
  placeholder,
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
          label={label}
          placeholder={placeholder}
          error={Boolean(fieldState.error)}
          helperText={fieldState.error?.message}
          fullWidth
          required
        />
      )}
    />
  );
};
