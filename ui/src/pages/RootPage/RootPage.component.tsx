import {Box, CssBaseline, Grid} from "@mui/material";
import {Outlet} from "react-router-dom";
import {Sidebar} from "../../components/Sidebar";
import {Footer} from "../../components/Footer";
import {useStyles} from "./RootPage.styles";

export function RootPage() {
  const { classes } = useStyles();
  return (<main>
    <CssBaseline />
    <Grid className={classes.gridContainer} container>
      <Grid item xs={12} md={3}>
        <Sidebar />
      </Grid>

      <Grid item xs={12} md={9}>
        <Box className={classes.configurationWrapper}>
          <Outlet/>
        </Box>
        <Box className={classes.footerWrapper}>
          <Footer />
        </Box>
      </Grid>
    </Grid>
  </main>);
}
