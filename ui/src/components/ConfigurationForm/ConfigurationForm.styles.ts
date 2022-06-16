import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles<{ isEmbedded: boolean }>()((theme, { isEmbedded }) => {
  // NOTE: if the form container is embedded we use different breakpoint for 2 columns variant
  const breakpoint = isEmbedded ? 500 : 'lg';

  return {
    formContainer: {
      display: 'grid',
      gridTemplateColumns: '1fr',
      columnGap: theme.spacing(3),
      rowGap: theme.spacing(0.5),

      [theme.breakpoints.up(breakpoint)]: {
        gridTemplateColumns: '1fr 1fr',
      },
    },

    formMetadataRow: {
      [theme.breakpoints.up(breakpoint)]: {
        gridRowStart: 1,
      },
    },
    formVersionsRow: {
      [theme.breakpoints.up(breakpoint)]: {
        gridRowStart: 2,
      },
    },
    formEffectsRow: {
      [theme.breakpoints.up(breakpoint)]: {
        gridRowStart: 3,
      },
    },
    formEndpointsRow: {
      [theme.breakpoints.up(breakpoint)]: {
        gridRowStart: 4,
      },
    },
    formEndpoints2ndRow: {
      [theme.breakpoints.up(breakpoint)]: {
        gridRowStart: 5,
      },
    },
    formActionsRow: {
      [theme.breakpoints.up(breakpoint)]: {
        gridColumnStart: 2,
        gridColumnEnd: 3,
        gridRowStart: 6,
      },
    },

    actionsContainer: {
      display: 'flex',
      justifyContent: 'flex-end',
      alignItems: 'center',
      '& button:first-child': {
        marginRight: theme.spacing(2),
      },
    },

    submitButton: {
      whiteSpace: 'nowrap',
    },
  };
});
