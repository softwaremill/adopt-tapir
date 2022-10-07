import { makeStyles } from 'tss-react/mui';

export const useStyles = makeStyles<{ level: number }>()((theme, { level }) => {
  return {
    wrapper: {
      height: '100%',
      overflow: 'auto',
    },
    nodeRoot: {
      paddingLeft: '0px',
      margin: '0px',
    },
    nodeRow: {
      listStyleType: 'none',
      position: 'relative',
    },
    nodeContent: {
      background: 'none',
      color: 'inherit',
      width: '100%',
      border: 'none',
      outline: 'inherit',
      display: 'flex',
      alignItems: 'center',
      verticalAlign: 'middle',
      cursor: 'pointer',
      padding: theme.spacing(0.75),
      paddingLeft: theme.spacing(level === 0 ? 1 : level * 3),
      textDecoration: 'none',
      ':hover': {
        backgroundColor: theme.palette.primary.light,
        backgroundClip: 'padding-box',
      },
    },
    openedFile: {
      color: theme.palette.primary.dark,
      backgroundColor: theme.palette.primary.light,
      backgroundClip: 'padding-box',
    },
  };
});
