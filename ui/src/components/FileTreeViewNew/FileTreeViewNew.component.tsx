import TreeView from '@mui/lab/TreeView';
import { Folder, FolderOpen, InsertDriveFileOutlined } from '@mui/icons-material';
import TreeItem from '@mui/lab/TreeItem';
import { Dispatch, SetStateAction, useEffect, useState } from 'react';

type TreeNode = {
  content: string | TreeNode;
  name: string;
  type: 'file' | 'directory';
  id?: string;
};

type OpenedFile = {
  name: string;
  content: string;
};

interface Props {
  tree?: TreeNode[];
  setOpenedFile: Dispatch<SetStateAction<OpenedFile>>;
  allUniqueIds: string[];
}

const renderTree = (node: TreeNode) => {
  return (
    <TreeItem key={node.name} nodeId={node.id ?? node.name} label={node.name}>
      {Array.isArray(node.content) ? node.content.map(deeperNode => renderTree(deeperNode)) : null}
    </TreeItem>
  );
};

function findNestedObj(entireObj: TreeNode[], id: string): OpenedFile {
  let foundObj: OpenedFile = { name: '', content: '' };
  JSON.stringify(entireObj, (_, nestedValue) => {
    if (nestedValue && nestedValue.id === id) {
      foundObj = { name: nestedValue.name, content: nestedValue.content };
    }
    return nestedValue;
  });
  return foundObj;
}

export const FileTreeViewNew = ({ tree, setOpenedFile, allUniqueIds }: Props) => {
  const handleSelect = (event: React.SyntheticEvent, nodeId: string) => {
    if (tree) {
      const node = findNestedObj(tree, nodeId);
      if (node && typeof node.content === 'string') setOpenedFile({ name: node.name, content: node.content });
    }
  };

  return (
    <TreeView
      aria-label="file tree view"
      defaultEndIcon={<InsertDriveFileOutlined />}
      defaultCollapseIcon={<FolderOpen />}
      defaultExpandIcon={<Folder />}
      defaultExpanded={allUniqueIds}
      onNodeSelect={handleSelect}
      sx={{ height: '100%', flexGrow: 1, overflowY: 'auto' }}
    >
      {tree && tree[0] && tree.map((node: TreeNode, index: number) => <div key={index}>{renderTree(node)}</div>)}
    </TreeView>
  );
};
