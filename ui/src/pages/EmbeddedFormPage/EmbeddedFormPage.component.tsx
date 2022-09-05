import { embeddedTheme } from '../../theme';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { ConfigurationForm } from '../../components/ConfigurationForm';

export function EmbeddedFormPage() {
  return (
    <ThemeProvider theme={embeddedTheme}>
      <CssBaseline />
      <ConfigurationForm isEmbedded />
    </ThemeProvider>
  );
}
