import TreeView from '@mui/lab/TreeView';
import { Folder, InsertDriveFileOutlined, FolderOpenTwoTone } from '@mui/icons-material';
import TreeItem from '@mui/lab/TreeItem';
import { Dispatch, SetStateAction, useEffect } from 'react';
import { TreeNode, Tree } from './FileTreeView.types';
import { findNestedNode, simulateMainNodeClick } from './FileTreeView.utils';

interface Props {
  tree?: Tree;
  setOpenedFile: Dispatch<SetStateAction<TreeNode>>;
  allUniqueIds: string[];
  mainNodeId: string;
}

const renderTree = (node: TreeNode) => {
  return (
    <TreeItem key={node.name} nodeId={node.id ?? node.name} label={node.name}>
      {Array.isArray(node.content) ? node.content.map(deeperNode => renderTree(deeperNode)) : null}
    </TreeItem>
  );
};

export const FileTreeView = ({ tree, setOpenedFile, allUniqueIds, mainNodeId }: Props) => {
  const handleSelect = (event: React.SyntheticEvent, nodeId: string) => {
    if (tree) {
      const node = findNestedNode(tree, nodeId);
      if (typeof node.content === 'string') setOpenedFile(node);
    }
  };

  useEffect(() => {
    simulateMainNodeClick(mainNodeId);
  }, [mainNodeId]);

  return (
    <TreeView
      aria-label="file tree view"
      defaultEndIcon={<InsertDriveFileOutlined />}
      defaultCollapseIcon={<FolderOpenTwoTone />}
      defaultExpandIcon={<Folder />}
      defaultExpanded={allUniqueIds}
      onNodeSelect={handleSelect}
      sx={{ flexGrow: 1, display: 'inline-flex', flexDirection: 'column' }}
    >
      {tree && tree[0] && tree.map((node: TreeNode, index: number) => <div key={index}>{renderTree(node)}</div>)}
    </TreeView>
  );
};
