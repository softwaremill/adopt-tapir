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

    groupedInputs: {
      display: 'flex',
      flexDirection: 'column',
      border: 0,
      paddingTop: 0,
    },

    groupLegend: {
      float: 'left',
      color: theme.palette.secondary.main,
      borderBottom: '1px solid ' + theme.palette.divider,
      marginBottom: theme.spacing(1),
    },

    formActionsRow: {
      [theme.breakpoints.up(breakpoint)]: {
        gridColumnStart: 2,
        gridColumnEnd: 3,
        gridRowStart: 6,
      },
    },

    actionsContainer: {
      justifySelf: 'end',

      '& button:not(:last-child)': {
        marginRight: theme.spacing(2),
      },
    },

    submitButton: {
      whiteSpace: 'nowrap',
    },

    inputWithAddon: {
      display: 'flex',
      alignItems: 'center',
    },

    serverTooltip: {
      display: 'flex',
      alignItems: 'center',
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      '& svg': {
        color: theme.palette.secondary.main,
      },
      ':hover': {
        '& svg': {
          color: theme.palette.primary.main,
        },
      },
      margin: '16px 0 8px',
    },

    headingWrapper: {
      marginBottom: '1em',
      flexDirection: 'row',
      alignItems: 'center',
    },
  };
});
