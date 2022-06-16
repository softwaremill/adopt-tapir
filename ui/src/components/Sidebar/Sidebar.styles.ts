import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles()(theme => ({
  sidebarContainer: {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    alignItems: 'center',
    height: '100%',
    backgroundColor: theme.palette.secondary.main,

    [theme.breakpoints.up('md')]: {
      borderRightWidth: '2px',
      borderRightStyle: 'solid',
      borderRightColor: theme.palette.primary.main,
    },
  },

  figureWrapper: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },

  infoText: {
    padding: theme.spacing(3),
    textAlign: 'center',
    color: theme.palette.neutral.main,
  },

  figure: {
    width: '100%',
    maxWidth: 260,
    marginTop: theme.spacing(8),
  },

  figcaption: {
    fontSize: theme.typography.pxToRem(64),
    fontWeight: theme.typography.fontWeightLight,
    color: theme.palette.neutral.main,
    textAlign: 'center',
  },

  social: {
    color: theme.palette.neutral.main,
    marginBottom: theme.spacing(1),
  },
}));
