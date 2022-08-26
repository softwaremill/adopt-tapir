import {CssBaseline, Grid} from "@mui/material";
import {Outlet} from "react-router-dom";
import {useStyles} from "../App.styles";

export function RootPage() {
  const { classes } = useStyles();
  return (<main>
    <CssBaseline />
    <Grid className={classes.gridContainer} container>
      <Outlet/>
    </Grid>
  </main>);
}
