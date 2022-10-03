import { useEffect, useState } from 'react';
import { parse } from 'query-string';
import { starterValidationSchema } from '../components/ConfigurationForm/ConfigurationForm.consts';
import { StarterRequest } from '../api/starter';
import { SnackbarConfig } from '../components/CommonSnackbar';
import { useSearchParams } from 'react-router-dom';

export function useSharedConfig(): [StarterRequest | undefined, SnackbarConfig | undefined, boolean, boolean] {
  const [searchParams, setSearchParams] = useSearchParams();
  const [request, setRequest] = useState<StarterRequest>();
  const [snackbar, setSnackbar] = useState<SnackbarConfig>();
  const [ready, setReady] = useState(false);
  const [preview, setPreview] = useState(false);

  useEffect(() => {
    if (searchParams.toString() === '') {
      setReady(true);
      return;
    }
    const params = parse(searchParams.toString(), { parseBooleans: true });
    const shouldPreview = 'preview' in params;
    setPreview(shouldPreview);
    starterValidationSchema
      .isValid(params)
      .then(isValid => {
        if (isValid) {
          setRequest(params as StarterRequest);
          if (!shouldPreview) {
            setSnackbar({
              open: true,
              severity: 'info',
              message: 'Linked configuration was applied.',
            });
          }
        } else {
          setSnackbar({
            open: true,
            severity: 'warning',
            message: 'Linked configuration is not valid therefore it was not applied.',
          });
        }
      })
      .catch(_ => {
        setSnackbar({
          open: true,
          severity: 'warning',
          message: 'Validation of linked configuration failed therefore it was not applied.',
        });
      })
      .finally(() => {
        setReady(true);
      });
    setSearchParams({});
  }, [searchParams, setSearchParams]);

  return [request, snackbar, ready, preview];
}
