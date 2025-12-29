import { Typography, Link } from '@mui/material';
import SoftwareMillLogo from '@/assets/sml-logo-1024.png';
import { useStyles } from './Footer.styles';

export const Footer: React.FC = () => {
  const { classes } = useStyles();

  return (
    <>
      <Typography variant="overline">Created by SoftwareMill</Typography>
      <Link href="https://softwaremill.com/" target="_blank" rel="noopener">
        <img className={classes.footerLogo} src={SoftwareMillLogo} alt="SoftwareMill logo" />
      </Link>
    </>
  );
};
