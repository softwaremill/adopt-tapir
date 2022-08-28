import {makeStyles} from "tss-react/mui";

export const useStyles = makeStyles<{level: number}>()((theme, {level}) => {
  return {
    wrapper: {
      height: '100%',
      overflowX: 'auto',
      padding: '2px'
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
      padding: '4px 4px',
      paddingLeft: `${level * 24}px`,
      color: 'inherit',
      textDecoration: 'none',
      ":hover": {
        backgroundColor: theme.palette.primary.light,
        backgroundClip: "padding-box"
      }
    },
    openedFile: {
      fontWeight: "bold",
      color: theme.palette.accent.main
    }
  };
});
