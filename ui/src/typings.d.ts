import {} from './package.json';

declare module '@mui/material/styles' {
  interface Palette {
    accent: Palette['primary'];
    neutral: Palette['primary'];
  }
  interface PaletteOptions {
    accent: PaletteOptions['primary'];
    neutral: PaletteOptions['primary'];
  }
}
