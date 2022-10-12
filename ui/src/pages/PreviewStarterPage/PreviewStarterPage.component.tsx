import { v4 as uuid } from 'uuid';
import { Box, Grid } from '@mui/material';
import { useContext, useEffect, useState } from 'react';
import { FileContentView } from 'components/FileContentView';
import { FileTreeView } from 'components/FileTreeView/FileTreeView.component';
import { Tree } from 'components/FileTreeView/FileTreeView.types';
import { DEFAULT_NODE } from 'components/FileTreeView/FileTreeView.utils';
import { useNavigate } from 'react-router-dom';
import { doRequestPreview } from '../../api/starter';
import { useApiCall } from '../../hooks/useApiCall';
import { ApiCallAddons } from '../../components/ApiCallAddons';
import { PreviewStarterButtons } from '../../components/PreviewStarterButtons';
import { useStyles } from './PreviewStarterPage.styles';
import { ConfigurationDataContext } from '../../contexts';

export function PreviewStarterPage() {
  const navigate = useNavigate();
  const [{ formData }] = useContext(ConfigurationDataContext);
  const { classes, cx } = useStyles();
  const [tree, setTree] = useState<Tree>();
  const { call, clearError, isLoading, errorMessage } = useApiCall();
  const [mainNodeId, setMainNodeId] = useState('');
  const [openedFile, setOpenedFile] = useState(DEFAULT_NODE);

  const allUniqueIds: string[] = [];

  const addIds = (tree: Tree): Tree => {
    return tree.map(file => {
      if (file.id === undefined) {
        const uniqueId = uuid();
        file.id = uniqueId;
        allUniqueIds.push(uniqueId);
        if (file.name === 'Main.scala') {
          setMainNodeId(uniqueId);
        }
      }
      if (Array.isArray(file.content)) {
        addIds(file.content);
      }
      return file;
    });
  };

  const setParsedFiles = (tree: Tree): void => {
    const treeWithIds = addIds(tree);
    setTree(treeWithIds);
  };

  useEffect(() => {
    if (formData === undefined) {
      navigate('/');
    } else {
      call(() => doRequestPreview(formData, setParsedFiles));
    }
  }, [formData, navigate, call]);

  // 92.5px is the height of buttons panel.
  return (
    <>
      <Grid container style={{ height: 'calc(100vh - (100vh * 0.08) - 150px)', minHeight: '450px' }}>
        <Grid item xs={2.4} className={classes.fullHeight}>
          <Box className={cx(classes.fullHeight, classes.treeViewContainer)}>
            <FileTreeView
              tree={tree}
              setOpenedFile={setOpenedFile}
              allUniqueIds={allUniqueIds}
              mainNodeId={mainNodeId}
            />
          </Box>
        </Grid>
        <Grid item xs={9.6} className={classes.fullHeight}>
          <Box className={cx(classes.fullHeight, classes.fileViewContainer)}>
            <FileContentView openedFile={openedFile} />
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
