import { Typography, IconButton, Link, Tooltip } from '@mui/material';
import { GitHub, Article } from '@mui/icons-material';
import TapirLogo from '@/assets/tapir-hex.svg?react';
import { useStyles } from './Sidebar.styles';

export const Sidebar = () => {
  const { classes } = useStyles();

  return (
    <aside className={classes.sidebarContainer}>
      <div className={classes.figureWrapper}>
        <figure className={classes.figure}>
          <TapirLogo />
          <Typography className={classes.figcaption} component="figcaption">
            tapir
          </Typography>
        </figure>

        <Typography className={classes.infoText} variant="subtitle1" component="h1">
          Tapir provides a programmer-friendly, reasonably type-safe API to expose, consume and document HTTP endpoints,
          using the Scala language.
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
