// NOTE: empty import here is needed in order to change this file into module so we can use
// module agumentation not module declaration
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
