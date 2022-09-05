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

    formFirstRow: {
      [theme.breakpoints.up(breakpoint)]: {
        gridRowStart: 1,
      },
    },
    formSecondRow: {
      alignSelf: 'center',

      [theme.breakpoints.up(breakpoint)]: {
        gridRowStart: 2,
      },
    },
    formThirdRow: {
      alignSelf: 'center',

      [theme.breakpoints.up(breakpoint)]: {
        gridRowStart: 3,
      },
    },
    formFourthRow: {
      [theme.breakpoints.up(breakpoint)]: {
        gridRowStart: 4,
      },
    },
    formFifthRow: {
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
      justifySelf: 'end',

      '& button:not(:last-child)': {
        marginRight: theme.spacing(2),
      },
    },

    submitButton: {
      whiteSpace: 'nowrap',
    },
  };
});
