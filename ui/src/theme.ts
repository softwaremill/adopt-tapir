import { createTheme, responsiveFontSizes } from '@mui/material/styles';

const baseTheme = createTheme({
  palette: {
    primary: {
      main: '#f3705e',
      light: '#fac0b8',
      dark: '#d62810',
    },
    secondary: {
      main: '#55494b',
      light: '#c4babc',
    },
    accent: {
      main: '#dc2855',
    },
    neutral: {
      main: '#f5f5f5',
    },
    divider: 'rgba(0, 0, 0, 0.12)',
  },
  components: {
    MuiButton: {
      styleOverrides: {
        sizeMedium: {
          paddingLeft: 14,
          paddingRight: 14,
        },
      },
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
