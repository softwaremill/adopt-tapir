import {makeStyles} from "tss-react/mui";

export const useStyles = makeStyles<{level: number}>()((theme, {level}) => {
  return {
    wrapper: {
      height: '100%',
      overflow: 'auto',
    },
    nodeRoot: {
      paddingLeft: '0px',
      margin: '0px'

    },
    nodeRow: {
      listStyleType: 'none',
      position: "relative"
    },
    nodeContent: {
      width: 'auto',
      display: 'flex',
      alignItems: 'center',
      verticalAlign: 'middle',
      padding: theme.spacing(0.75),
      paddingLeft: theme.spacing(level === 0 ? 1 : level * 3),
      color: 'inherit',
      textDecoration: 'none',
      ":hover": {
        backgroundColor: theme.palette.primary.light,
        backgroundClip: "padding-box"
      }
    },
    openedFile: {
      fontWeight: "bold",
      color: theme.palette.primary.dark,
      backgroundColor: theme.palette.primary.light,
      backgroundClip: "padding-box"
    }
  };
});
