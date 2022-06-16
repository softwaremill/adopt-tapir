import { createTheme, responsiveFontSizes } from '@mui/material/styles';

const baseTheme = createTheme({
  palette: {
    primary: {
      main: '#f3705e',
    },
    secondary: {
      main: '#55494b',
    },
    accent: {
      main: '#dc2855',
    },
    neutral: {
      main: '#f5f5f5',
    },
  },
});

export const theme = responsiveFontSizes(baseTheme);

export const embeddedTheme = createTheme(theme, {
  palette: {
    background: {
      default: 'initial',
    },
  },
});
