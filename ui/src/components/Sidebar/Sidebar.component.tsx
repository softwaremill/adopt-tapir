import { Typography, IconButton, Link, Tooltip } from '@mui/material';
import { GitHub, Article } from '@mui/icons-material';
import { ReactComponent as TapirLogo } from 'assets/tapir-hex.svg';
import { useStyles } from './Sidebar.styles';

export const Sidebar = () => {
  const { classes } = useStyles();

  return (
    <aside className={classes.sidebarContainer}>
      <div className={classes.figureWrapper}>
        <figure className={classes.figure}>
          <TapirLogo />
          <Typography className={classes.figcaption} component="figcaption">
            tAPIr
          </Typography>
        </figure>

        <Typography className={classes.infoText} variant="subtitle1">
          With tapir, you can describe HTTP API endpoints as immutable Scala values. Each endpoint can contain a number
          of input and output parameters.
        </Typography>
      </div>

      <div className={classes.social}>
        <Tooltip title="Source code" placement="top">
          <Link href="https://github.com/softwaremill/tapir" target="_blank" rel="noopener" color="inherit">
            <IconButton color="inherit">
              <GitHub fontSize="large" />
            </IconButton>
          </Link>
        </Tooltip>

        <Tooltip title="Documentation" placement="top">
          <Link href="https://tapir.softwaremill.com/" target="_blank" rel="noopener" color="inherit">
            <IconButton color="inherit">
              <Article fontSize="large" />
            </IconButton>
          </Link>
        </Tooltip>
      </div>
    </aside>
  );
};
