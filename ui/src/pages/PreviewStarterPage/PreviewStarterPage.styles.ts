import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles()(theme => ({
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
