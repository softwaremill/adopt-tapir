import { TextField } from '@mui/material';
import { Controller, useFormContext } from 'react-hook-form';
import {useCallback} from "react";

interface FormTextFieldProps {
  className?: string;
  name: string;
  label: string;
  placeholder?: string;
  defaultValue?: string;
  selectOnClick?: boolean;
}

export const FormTextField: React.FC<FormTextFieldProps> = ({
  className,
  name,
  label,
  placeholder,
  defaultValue = '',
  selectOnClick = false
}) => {
  const { control } = useFormContext();
  const onClick = useCallback((target: HTMLInputElement) => {
    if (selectOnClick) {
      return target.select();
    }
  }, [selectOnClick]);
  return (
    <Controller
      name={name}
      control={control}
      defaultValue={defaultValue}
      render={({ field, fieldState }) => (
        <TextField
          {...field}
          onClick={e => onClick(e.target as HTMLInputElement)}
          className={className}
          variant="outlined"
          size="small"
          margin="normal"
          color="secondary"
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
