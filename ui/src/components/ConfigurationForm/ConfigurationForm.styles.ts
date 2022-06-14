import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles()(theme => ({
  formContainer: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    columnGap: theme.spacing(3),
    rowGap: theme.spacing(0.5),
  },
  formMetadataRow: {
    gridRowStart: 1,
  },
  formVersionsRow: {
    gridRowStart: 2,
  },
  formEffectsRow: {
    gridRowStart: 3,
  },
  formEndpointsRow: {
    gridRowStart: 4,
  },
  formEndpoints2ndRow: {
    gridRowStart: 5,
  },
  formActionsRow: {
    gridColumnStart: 2,
    gridColumnEnd: 3,
    gridRowStart: 6,
  },
  actionsContainer: {
    display: 'flex',
    justifyContent: 'flex-end',
    alignItems: 'center',
    '& button:first-child': {
      marginRight: theme.spacing(2),
    },
  },
}));
