import {DirNode, FileNode, FileTree, TreeState} from "./FileTreeView.types";
import {useStyles} from "./FileTreeView.styles";
import {Folder, InsertDriveFileOutlined} from "@mui/icons-material";
import {NodeAbsoluteLocation} from "./FileTreeView.utils";
import {useEffect, useState} from "react";
import {theme} from "../../theme";

type Props = {
  tree: FileTree,
  state: TreeState,
  location: NodeAbsoluteLocation
}

export function FileTreeView({tree, location, state}: Props) {
  return (<>
    <div>
      {tree.map((node, index) => node.type === 'directory'
        ? (<DirNodeView key={'node-' + index} node={node} location={location} state={state}/>)
        : (<FileNodeView key={'node-' + index} node={node} location={location} state={state}/>))}
    </div>
  </>);
}

type NodeProps<T> = {
  node: T,
  location: NodeAbsoluteLocation,
  state: TreeState
}

function FileNodeView({node: {name}, location, state}: NodeProps<FileNode>) {
  const {classes} = useStyles()
  return (<div className={classes.nodeRow} onClick={() => state.openFile(location.add(name))}>
    <span><InsertDriveFileOutlined/>{name}</span>
  </div>);
}

function DirNodeView({node: {name, children}, location, state}: NodeProps<DirNode>) {
  const {classes} = useStyles()
  const [dirLocation, setDirLocation] = useState(location.add(name))
  const [opened, setOpened] = useState(state.isDirOpened(dirLocation))

  useEffect(() => {
    setDirLocation(location.add(name))
  }, [location, name])

  useEffect(() => {
    setOpened(state.isDirOpened(dirLocation))
  }, [dirLocation, state])

  return (<>
    <div className={classes.nodeRow} onClick={() => state.toggleDir(dirLocation)}>
      <Folder/>{name}
    </div>
    <div className={classes.dirChildren} style={opened ? {} : {display: "none"}}>
      <FileTreeView
        tree={children}
        location={dirLocation}
        state={state}/>
    </div>
  </>);
}
