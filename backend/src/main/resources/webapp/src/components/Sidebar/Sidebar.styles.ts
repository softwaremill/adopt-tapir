import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles()(theme => ({
  sidebarContainer: {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    alignItems: 'center',
    width: '100%',
    height: '100%',
    backgroundColor: theme.palette.secondary.main,
    borderRightWidth: '2px',
    borderRightStyle: 'solid',
    borderRightColor: theme.palette.primary.main,
  },

  figure: {
    width: '50%',
    marginTop: theme.spacing(8),
    pointerEvents: 'none',
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
