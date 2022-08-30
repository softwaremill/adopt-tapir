import { makeStyles } from 'tss-react/mui';
import { darken } from '@mui/system/colorManipulator';

export const useStyles = makeStyles()(theme => ({
  gridContainer: {
    height: '100vh',
  },

  configurationWrapper: {
    height: '92%',
    backgroundColor: theme.palette.neutral.main,
    padding: theme.spacing(8, 3),

    [theme.breakpoints.up('sm')]: {
      padding: theme.spacing(8, 10),
    },

    [theme.breakpoints.up('xl')]: {
      padding: theme.spacing(8, 16),
    },
  },

  configurationPaper: {
    padding: theme.spacing(3),
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

  fullHeight: {
    height: '100%'
  },

  treeViewContainer: {
    borderTopLeftRadius: '4px',
    border: '1px solid ' + theme.palette.divider,
    backgroundColor: 'white',
  },

  fileViewContainer: {
    marginLeft: theme.spacing(1),
    width: `calc(100% - ${theme.spacing(1)})`,
    backgroundColor: 'white',
    border: '1px solid ' + theme.palette.divider,
    borderTopRightRadius: '4px'
  },

  fileViewButtonsContainer: {
    display: 'flex',
    marginTop: theme.spacing(1),
    backgroundColor: 'white',
    padding: theme.spacing(3),
    border: '1px solid ' + theme.palette.divider,
    borderBottomLeftRadius: '4px',
    borderBottomRightRadius: '4px',
    justifyContent: "center",
    '& button:first-child': {
      marginRight: theme.spacing(2),
    },
  }
}));
