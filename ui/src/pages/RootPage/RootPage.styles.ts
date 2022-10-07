import { makeStyles } from 'tss-react/mui';
import { darken } from '@mui/system/colorManipulator';

export const useStyles = makeStyles()(theme => ({
  gridContainer: {
    height: '100vh',
  },

  configurationWrapper: {
    height: '92%',
    backgroundColor: theme.palette.neutral.main,
    padding: theme.spacing(5, 2),

    [theme.breakpoints.up('sm')]: {
      padding: theme.spacing(5, 3),
    },

    [theme.breakpoints.up('xl')]: {
      padding: theme.spacing(5, 5),
    },
  },

  footerWrapper: {
    height: '8%',
    backgroundColor: darken(theme.palette.neutral.main, 0.1),
    color: theme.palette.secondary.main,
    display: 'flex',
    justifyContent: 'flex-end',
    alignItems: 'center',
    padding: theme.spacing(2),
  },
}));
