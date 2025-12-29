import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles()(() => {
  return {
    wrapper: {
      height: '100%',
      marginTop: 0,
      overflow: 'auto',
    },
  };
});
