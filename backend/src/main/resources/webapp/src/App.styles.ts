import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles()(theme => ({
  configurationWrapper: {
    height: '100%',
    backgroundColor: theme.palette.neutral.main,
    padding: theme.spacing(8, 20),
  },
}));
