import {makeStyles} from "tss-react/mui";

export const useStyles = makeStyles()((theme, ) => {
  return {
    dirChildren: {
      paddingLeft: '24px'
    },
    nodeRow: {
      verticalAlign: 'middle',
      ":hover": {
        backgroundColor: "yellow"
      }
    },

  };
});
