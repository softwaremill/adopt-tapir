import { makeStyles } from 'tss-react/mui';
import { darken } from '@mui/system/colorManipulator';

export const useStyles = makeStyles()(theme => ({
  configurationWrapper: {
    height: '92%',
    backgroundColor: theme.palette.neutral.main,
    padding: theme.spacing(8, 20),
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
