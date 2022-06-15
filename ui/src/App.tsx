import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { CssBaseline, Grid, Paper, Box, Container, ThemeProvider } from '@mui/material';
import { Sidebar } from 'components/Sidebar';
import { ConfigurationForm } from 'components/ConfigurationForm';
import { Footer } from 'components/Footer';
import { embeddedTheme } from './theme';

import { useStyles } from './App.styles';

export const App: React.FC = () => {
  const { classes } = useStyles();

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            <main>
              <CssBaseline />

              <Grid className={classes.gridContainer} container>
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
              </Grid>
            </main>
          }
        />

        <Route
          path="/embedded-form"
          element={
            <ThemeProvider theme={embeddedTheme}>
              <CssBaseline />
              <ConfigurationForm isEmbedded />
            </ThemeProvider>
          }
        />

        <Route path="*" element={<Navigate to="/" replace={true} />} />
      </Routes>
    </BrowserRouter>
  );
};
