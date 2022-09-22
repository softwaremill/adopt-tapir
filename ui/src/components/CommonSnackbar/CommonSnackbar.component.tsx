import { Alert, AlertColor, Snackbar } from '@mui/material';

type Props = {
  onClose: () => void;
  open: boolean;
  duration?: number;
  message?: String;
  severity?: AlertColor;
};

export type SnackbarConfig = {
  open: boolean,
  severity?: AlertColor,
  message?: String
}

export function CommonSnackbar({ onClose, open, severity, message, duration }: Props) {
  return (
    <>
      <Snackbar
        open={open}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        autoHideDuration={duration || 5000}
        onClose={onClose}
      >
        <Alert onClose={onClose} severity={severity || 'error'} sx={{ width: '100%' }}>
          {message}
        </Alert>
      </Snackbar>
    </>
  );
}
