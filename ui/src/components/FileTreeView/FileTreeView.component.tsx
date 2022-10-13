import { v4 as uuid } from 'uuid';
import TreeView from '@mui/lab/TreeView';
import { Folder, InsertDriveFileOutlined, FolderOpenTwoTone } from '@mui/icons-material';
import TreeItem from '@mui/lab/TreeItem';
import { Dispatch, SetStateAction, useCallback, useEffect, useState } from 'react';
import { TreeNode, Tree } from './FileTreeView.types';
import { findNestedNode } from './FileTreeView.utils';

interface Props {
  tree?: Tree;
  setOpenedFile: Dispatch<SetStateAction<TreeNode>>;
}

const renderTree = (node: TreeNode) => {
  return (
    <TreeItem key={node.name} nodeId={node.id ?? node.name} label={node.name}>
      {Array.isArray(node.content) ? node.content.map(deeperNode => renderTree(deeperNode)) : null}
    </TreeItem>
  );
};

export const FileTreeView = ({ tree, setOpenedFile }: Props) => {
  const [mainNodeId, setMainNodeId] = useState('');
  const [uniqueIds, setUniqueIds] = useState(['']);
  const [parsedTree, setParsedTree] = useState<Tree | null>(null);

  const openFile = useCallback(
    (nodeId: string) => {
      if (tree) {
        const node = findNestedNode(tree, nodeId);
        if (typeof node.content === 'string') setOpenedFile(node);
      }
    },
    [tree, setOpenedFile]
  );

  const handleSelect = (event: React.SyntheticEvent, nodeId: string) => {
    openFile(nodeId);
  };

  useEffect(() => {
    const addAndSaveIds = (tree: Tree): Tree => {
      return tree.map(file => {
        if (file.id === undefined) {
          const uniqueId = uuid();
          file.id = uniqueId;
          setUniqueIds((oldIds: string[]) => [...oldIds, uniqueId]);
        }
        if (Array.isArray(file.content)) {
          addAndSaveIds(file.content);
        }
        return file;
      });
    };

    const findAndSetMainNodeId = (tree: Tree): void => {
      tree.forEach(file => {
        if (file.id !== undefined && file.name === 'Main.scala') {
          setMainNodeId(file.id);
        } else if (Array.isArray(file.content)) {
          findAndSetMainNodeId(file.content);
        }
      });
    };

    if (tree !== undefined && parsedTree === null) {
      const treeWithIds = addAndSaveIds(tree);
      findAndSetMainNodeId(treeWithIds);
      setParsedTree(treeWithIds);
    }
  }, [tree, parsedTree]);

  useEffect(() => {
    openFile(mainNodeId);
  }, [mainNodeId, openFile]);

  return (
    <>
      {parsedTree && (
        <TreeView
          aria-label="file tree view"
          defaultEndIcon={<InsertDriveFileOutlined />}
          defaultCollapseIcon={<FolderOpenTwoTone />}
          defaultExpandIcon={<Folder />}
          defaultExpanded={uniqueIds}
          defaultSelected={mainNodeId}
          onNodeSelect={handleSelect}
          sx={{ flexGrow: 1, display: 'inline-flex', flexDirection: 'column' }}
        >
          {parsedTree.map((node: TreeNode, index: number) => (
            <div key={index}>{renderTree(node)}</div>
          ))}
        </TreeView>
      )}
    </>
  );
};
