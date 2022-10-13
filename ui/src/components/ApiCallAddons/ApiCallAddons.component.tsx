import { Backdrop, CircularProgress } from '@mui/material';
import { CommonSnackbar } from '../CommonSnackbar';

type Props = {
  isLoading: boolean;
  clearError: () => void;
  errorMessage?: string;
};

export function ApiCallAddons({ isLoading, clearError, errorMessage }: Props) {
  return (
    <>
      <Backdrop open={isLoading}>
        <CircularProgress />
      </Backdrop>
      <CommonSnackbar onClose={clearError} open={Boolean(errorMessage)} />
    </>
  );
}
