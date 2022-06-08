import { CssBaseline, Grid, Paper, Box } from '@mui/material';
import { Sidebar } from 'components/Sidebar';
import { ConfigurationForm } from 'components/ConfigurationForm';
import { Footer } from 'components/Footer';

import { useStyles } from './App.styles';

// TODO LIST:
// add react router
// create a separate path that can be embedded in an iframe
// feature flags to hide some specific fields

// NICE TO HAVE:
// shrink sidebar button <- similar to navbars

export const App: React.FC = () => {
  const { classes } = useStyles();

  return (
    <main>
      <CssBaseline />

      <Grid container sx={{ height: '100vh' }}>
        <Grid item xs={3}>
          <Sidebar />
        </Grid>

        <Grid item xs={9}>
          <Box className={classes.configurationWrapper}>
            <Paper variant="outlined" sx={{ padding: 3 }}>
              <ConfigurationForm />
            </Paper>
          </Box>
          <Box className={classes.footerWrapper}>
            <Footer />
          </Box>
        </Grid>
      </Grid>
    </main>
  );
};
