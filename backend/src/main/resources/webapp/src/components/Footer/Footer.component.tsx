import { Typography } from '@mui/material';
import SoftwareMillLogo from 'assets/sml-logo-1024.png';
import { useStyles } from './Footer.styles';

export const Footer: React.FC = () => {
  const { classes } = useStyles();

  return (
    <>
      <Typography variant="overline">Created by SoftwareMill</Typography>
      <img className={classes.footerLogo} src={SoftwareMillLogo} alt="SoftwareMill logo" />
    </>
  );
};
