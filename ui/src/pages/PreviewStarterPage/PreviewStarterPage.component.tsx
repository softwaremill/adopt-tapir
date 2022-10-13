import { Box, Grid } from '@mui/material';
import {
  FileTree,
  FileTreeView,
  getAllDirectories,
  NodeAbsoluteLocation,
  RootNodeLocation,
} from '../../components/FileTreeView';
import { useContext, useEffect, useState } from 'react';
import { FileContentView } from '../../components/FileContentView';
import { useNavigate } from 'react-router-dom';
import { doRequestPreview } from '../../api/starter';
import { useApiCall } from '../../hooks/useApiCall';
import { ApiCallAddons } from '../../components/ApiCallAddons';
import { useTreeState } from '../../hooks/useTreeState';
import { PreviewStarterButtons } from '../../components/PreviewStarterButtons';
import { useStyles } from './PreviewStarterPage.styles';
import { ConfigurationDataContext } from '../../contexts';

export function PreviewStarterPage() {
  const navigate = useNavigate();
  const [{ formData }] = useContext(ConfigurationDataContext);
  const { classes, cx } = useStyles();
  const [files, setFiles] = useState<FileTree>();
  const treeState = useTreeState('README.md');
  const { call, clearError, isLoading, errorMessage } = useApiCall();

  useEffect(() => {
    if (formData === undefined) {
      navigate('/');
    } else {
      call(() => doRequestPreview(formData, setFiles));
    }
  }, [formData, navigate, call]);

  // function references were introduced so that 'useCallback' can depend on them
  const openFileRef = treeState.openFile;
  const toggleDirRef = treeState.toggleDir;
  useEffect(() => {
    if (files !== undefined) {
      // open up all 'src' dirs
      getAllDirectories([], files)
        .filter(slug => slug.length > 0 && slug[0] === 'src')
        .forEach(dir => toggleDirRef(new NodeAbsoluteLocation(...dir)));

      // select 'Main.scala' file
      if (formData !== undefined) {
        const mainScalaPath = ['src', 'main', 'scala', ...formData.groupId.split('.'), 'Main.scala'];
        openFileRef(new NodeAbsoluteLocation(...mainScalaPath));
      }
    }
  }, [files, formData, openFileRef, toggleDirRef]);

  // 92.5px is the height of buttons panel.
  return (
    <>
      <Grid container style={{ height: 'calc(100vh - (100vh * 0.08) - 150px)', minHeight: '450px' }}>
        <Grid item xs={2.4} className={classes.fullHeight}>
          <Box className={cx(classes.fullHeight, classes.treeViewContainer)}>
            <FileTreeView tree={files} location={RootNodeLocation} state={treeState} />
          </Box>
        </Grid>
        <Grid item xs={9.6} className={classes.fullHeight}>
          <Box className={cx(classes.fullHeight, classes.fileViewContainer)}>
            <FileContentView files={files} opened={treeState.openedFile} />
          </Box>
        </Grid>
        <Grid item xs={12} className={classes.fileViewButtonsContainer}>
          <PreviewStarterButtons apiCaller={call} request={formData} />
        </Grid>
      </Grid>
      <ApiCallAddons isLoading={isLoading} clearError={clearError} errorMessage={errorMessage} />
    </>
  );
}
