import {Alert, Backdrop, CircularProgress, Snackbar} from "@mui/material";

type Props = {
  isLoading: boolean,
  clearError: () => void,
  errorMessage?: string
}

export function ApiCallAddons({isLoading, clearError, errorMessage}: Props) {
  return (<>
    <Backdrop open={isLoading}>
      <CircularProgress />
    </Backdrop>
    <Snackbar
      open={Boolean(errorMessage)}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      autoHideDuration={5000}
      onClose={clearError}
    >
      <Alert severity="error" variant="outlined">
        {errorMessage}
      </Alert>
    </Snackbar>
  </>);
}
