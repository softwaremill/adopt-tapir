import { Container, Paper } from '@mui/material';
import { ConfigurationForm } from '../../components/ConfigurationForm';
import { useStyles } from './ConfigurationFormPage.styles';

export function ConfigurationFormPage() {
  const { classes } = useStyles();

  return (
    <>
      <Container maxWidth="lg" disableGutters>
        <Paper className={classes.configurationPaper} variant="outlined">
          <ConfigurationForm />
        </Paper>
      </Container>
    </>
  );
}
