import {Box, Grid} from "@mui/material";
import {useStyles} from "../App.styles";
import {FileTreeView} from "../components/FileTreeView/FileTreeView.component";
import {FileTree} from "../components/FileTreeView/FileTreeView.types";
import {useEffect, useState} from "react";
import {RootNodeLocation} from "../components/FileTreeView/FileTreeView.utils";
import {FileContentView} from "../components/FileContentView/FileContentView.component";
import {useLocation, useNavigate} from "react-router-dom";
import {doRequestPreview, StarterRequest} from "../api/starter";
import {useApiCall} from "../hooks/useApiCall";
import {ApiCallAddons} from "../components/ApiCallAddons/ApiCallAddons.component";
import {useTreeState} from "../hooks/useTreeState";
import {PreviewStarterButtons} from "../components/PreviewStarterButtons/PreviewStarterButtons.component";

export function PreviewStarterPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const {classes, cx} = useStyles();
  const [files, setFiles] = useState<FileTree>();
  const treeState = useTreeState("README.md");
  const [request, setRequest] = useState<StarterRequest>();
  const { call, clearError, isLoading, errorMessage } = useApiCall();

  useEffect(() => {
    if (!location.state) {
      navigate('/')
    } else {
      setRequest(location.state as StarterRequest);
    }
  }, [location]);

  useEffect(() => {
    if (request !== undefined) {
      call(() => doRequestPreview(request, setFiles))
    }
  }, [request]);

  // 92.5px is the height of buttons panel.
  return (
    <>
      <Grid container style={{height: 'calc(100% - 92.5px)'}}>
          <Grid item xs={3} className={classes.fullHeight}>
            <Box className={cx(classes.fullHeight, classes.treeViewContainer)}>
                <FileTreeView tree={files} location={RootNodeLocation} state={treeState}/>
            </Box>
          </Grid>
          <Grid item xs={9} className={classes.fullHeight}>
            <Box className={cx(classes.fullHeight, classes.fileViewContainer)}>
                <FileContentView files={files} opened={treeState.openedFile}/>
            </Box>
          </Grid>
          <Grid item xs={12} className={classes.fileViewButtonsContainer}>
              <PreviewStarterButtons apiCaller={call} request={request}/>
          </Grid>
      </Grid>
      <ApiCallAddons isLoading={isLoading} clearError={clearError} errorMessage={errorMessage}/>
    </>);
}
