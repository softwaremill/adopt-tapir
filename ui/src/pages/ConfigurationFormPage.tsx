import {Box, Container, Grid, Paper} from "@mui/material";
import {Sidebar} from "../components/Sidebar";
import {ConfigurationForm} from "../components/ConfigurationForm";
import {Footer} from "../components/Footer";
import {useStyles} from "../App.styles";

export function ConfigurationFormPage() {
  const { classes } = useStyles();

  return (<>
    <Grid item xs={12} md={3}>
      <Sidebar />
    </Grid>

    <Grid item xs={12} md={9}>
      <Box className={classes.configurationWrapper}>
        <Container maxWidth="lg" disableGutters>
          <Paper className={classes.configurationPaper} variant="outlined">
            <ConfigurationForm />
          </Paper>
        </Container>
      </Box>
      <Box className={classes.footerWrapper}>
        <Footer />
      </Box>
    </Grid>
  </>);
}
