import { v4 as uuid } from 'uuid';
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
import { FileTreeViewNew } from '../../components/FileTreeViewNew/FileTreeViewNew.component';
import { useNavigate } from 'react-router-dom';
import { doRequestPreview, doRequestPreviewNew } from '../../api/starter';
import { useApiCall } from '../../hooks/useApiCall';
import { ApiCallAddons } from '../../components/ApiCallAddons';
import { useTreeState } from '../../hooks/useTreeState';
import { PreviewStarterButtons } from '../../components/PreviewStarterButtons';
import { useStyles } from './PreviewStarterPage.styles';
import { ConfigurationDataContext } from '../../contexts';
import { AddToDriveSharp } from '@mui/icons-material';

type TreeNode = {
  content: string | TreeNode;
  name: string;
  type: 'file' | 'directory';
  id?: string;
};

export function PreviewStarterPage() {
  const navigate = useNavigate();
  const [{ formData }] = useContext(ConfigurationDataContext);
  const { classes, cx } = useStyles();
  const [files, setFiles] = useState<FileTree>();
  const [filesNew, setFilesNew] = useState<TreeNode[]>();
  const treeState = useTreeState('README.md');
  const { call, clearError, isLoading, errorMessage } = useApiCall();

  const [openedFile, setOpenedFile] = useState({ name: '', content: '' });

  useEffect(() => {
    if (formData === undefined) {
      navigate('/');
    } else {
      call(() => doRequestPreview(formData, setFiles));
    }
  }, [formData, navigate, call]);

  useEffect(() => {
    if (formData === undefined) {
      navigate('/');
    } else {
      call(() => doRequestPreviewNew(formData, setParsedFiles));
    }
  }, [formData, navigate, call]);

  const allUniqueIds: string[] = [];
  let mainNodeId = '';

  const addId = (files: TreeNode[]): TreeNode[] => {
    const a = files.map(file => {
      if (file.id === undefined) {
        const uniqueId = uuid();
        file.id = uniqueId;
        allUniqueIds.push(uniqueId);
        if (file.name === 'Main.scala') mainNodeId = mainNodeId;
      }
      if (Array.isArray(file.content)) {
        addId(file.content);
      }
      return file;
    });
    return a;
  };

  const setParsedFiles = (files: TreeNode[]): void => {
    addId(files);
    setFilesNew(files);
  };

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
            {/* <FileTreeView tree={files} location={RootNodeLocation} state={treeState} /> */}
            <FileTreeViewNew tree={filesNew} setOpenedFile={setOpenedFile} allUniqueIds={allUniqueIds} />
          </Box>
        </Grid>
        <Grid item xs={9.6} className={classes.fullHeight}>
          <Box className={cx(classes.fullHeight, classes.fileViewContainer)}>
            <FileContentView files={files} opened={treeState.openedFile} openedFile={openedFile} />
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
