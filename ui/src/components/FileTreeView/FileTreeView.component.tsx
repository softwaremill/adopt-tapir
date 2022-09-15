import { DirNode, FileNode, FileTree, TreeState } from './FileTreeView.types';
import { useStyles } from './FileTreeView.styles';
import { FolderOpenTwoTone, FolderTwoTone, InsertDriveFileOutlined } from '@mui/icons-material';
import { NodeAbsoluteLocation } from './FileTreeView.utils';
import { useEffect, useState } from 'react';

type Props = {
  tree?: FileTree;
  state: TreeState;
  location: NodeAbsoluteLocation;
};

export function FileTreeView(props: Props) {
  const { classes } = useStyles({ level: 0 });

  return (
    <div className={classes.wrapper}>
      <NodesView {...props} />
    </div>
  );
}

function NodesView({ tree, location, state }: Props) {
  const { classes } = useStyles({ level: location.getLevel() });
  const [opened, setOpened] = useState(state.isDirOpened(location));
  useEffect(() => {
    setOpened(state.isDirOpened(location));
  }, [location, state]);
  return (
    <ul className={classes.nodeRoot} style={opened ? {} : { display: 'none' }}>
      {tree &&
        tree.map((node, index) =>
          node.type === 'directory' ? (
            <DirNodeView key={'node-' + index} node={node} location={location} state={state} />
          ) : (
            <FileNodeView key={'node-' + index} node={node} location={location} state={state} />
          )
        )}
    </ul>
  );
}

type NodeProps<T> = {
  node: T;
  location: NodeAbsoluteLocation;
  state: TreeState;
};

function FileNodeView({ node: { name }, location, state }: NodeProps<FileNode>) {
  const { classes, cx } = useStyles({ level: location.getLevel() });
  const [fileLocation, setFileLocation] = useState(location.add(name));
  const [isOpened, setOpened] = useState(fileLocation.isSameAs(state.openedFile));

  useEffect(() => {
    setFileLocation(location.add(name));
  }, [location, name]);

  useEffect(() => {
    setOpened(fileLocation.isSameAs(state.openedFile));
  }, [fileLocation, name, state]);

  return (
    <li className={classes.nodeRow} onClick={() => state.openFile(location.add(name))}>
      <button
        className={isOpened ? cx(classes.nodeContent, classes.openedFile) : classes.nodeContent}
        onClick={e => e.preventDefault()}
      >
        <InsertDriveFileOutlined />
        {name}
      </button>
    </li>
  );
}

function DirNodeView({ node: { name, content }, location, state }: NodeProps<DirNode>) {
  const [dirLocation, setDirLocation] = useState(location.add(name));
  const { classes } = useStyles({ level: location.getLevel() });

  useEffect(() => {
    setDirLocation(location.add(name));
  }, [location, name]);

  return (
    <>
      <li className={classes.nodeRow}>
        <button
          className={classes.nodeContent}
          onClick={e => {
            e.preventDefault();
            e.stopPropagation();
            state.toggleDir(dirLocation);
          }}
        >
          {state.isDirOpened(dirLocation) ? <FolderTwoTone /> : <FolderOpenTwoTone />}
          {name}
        </button>
        <NodesView tree={content} location={dirLocation} state={state} />
      </li>
    </>
  );
}
