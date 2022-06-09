import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles()(theme => ({
  formContainer: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    // gridTemplateRows: 'repeat(5, 1fr)',
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
  formActionsRow: {
    gridColumnStart: 2,
    gridColumnEnd: 3,
    gridRowStart: 5,
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
