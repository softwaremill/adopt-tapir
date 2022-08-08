import { useState } from 'react';

export const useApiCall = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const clearError = () => {
    setErrorMessage('');
  };

  const call = async (apiCall: () => Promise<void>) => {
    try {
      clearError();
      setIsLoading(true);

      await apiCall();
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

  return {
    call,
    clearError,
    isLoading,
    errorMessage,
  };
};
