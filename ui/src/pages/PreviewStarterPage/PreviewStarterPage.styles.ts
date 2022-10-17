import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles()(theme => ({
  fullHeight: {
    height: '100%',
  },

  treeViewContainer: {
    borderTopLeftRadius: '4px',
    border: '1px solid ' + theme.palette.divider,
    backgroundColor: 'white',
    overflow: 'auto',
  },

  fileViewContainer: {
    marginLeft: theme.spacing(1),
    width: `calc(100% - ${theme.spacing(1)})`,
    backgroundColor: 'white',
    border: '1px solid ' + theme.palette.divider,
    borderTopRightRadius: '4px',
  },

  fileViewButtonsContainer: {
    display: 'flex',
    marginTop: theme.spacing(1),
    backgroundColor: 'white',
    padding: theme.spacing(2),
    border: '1px solid ' + theme.palette.divider,
    borderBottomLeftRadius: '4px',
    borderBottomRightRadius: '4px',
    justifyContent: 'center',
    '& button:not(:last-child)': {
      marginRight: theme.spacing(2),
    },
  },
}));
