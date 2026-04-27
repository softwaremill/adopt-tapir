import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles()(theme => {
  return {
    wrapper: {
      position: 'relative',
      height: '100%',
      marginTop: 0,
      overflow: 'auto',
    },
    copyButton: {
      position: 'absolute',
      top: theme.spacing(0.5),
      right: theme.spacing(3),
      zIndex: 1,
      backgroundColor: 'rgba(255, 255, 255, 0.85)',
      '&:hover': {
        backgroundColor: 'rgba(255, 255, 255, 1)',
      },
    },
  };
});
