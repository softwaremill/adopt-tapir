import { Typography, IconButton, Link } from '@mui/material';
import { GitHub, Article } from '@mui/icons-material';
import { ReactComponent as TapirLogo } from 'assets/tapir-hex.svg';
import { useStyles } from './Sidebar.styles';

export const Sidebar = () => {
  const { classes } = useStyles();

  return (
    <aside className={classes.sidebarContainer}>
      <figure className={classes.figure}>
        <TapirLogo />
        <Typography className={classes.figcaption} component="figcaption">
          tAPIr
        </Typography>
      </figure>

      <div className={classes.social}>
        <Link href="https://github.com/softwaremill/tapir" target="_blank" rel="noopener" color="inherit">
          <IconButton color="inherit">
            <GitHub fontSize="large" />
          </IconButton>
        </Link>
        <Link href="https://tapir.softwaremill.com/" target="_blank" rel="noopener" color="inherit">
          <IconButton color="inherit">
            <Article fontSize="large" />
          </IconButton>
        </Link>
      </div>
    </aside>
  );
};
