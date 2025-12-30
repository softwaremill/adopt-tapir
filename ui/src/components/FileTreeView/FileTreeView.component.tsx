import { v4 as uuid } from 'uuid';
import { SimpleTreeView } from '@mui/x-tree-view/SimpleTreeView';
import { TreeItem } from '@mui/x-tree-view/TreeItem';
import { Folder, InsertDriveFileOutlined, FolderOpenTwoTone } from '@mui/icons-material';
import { Dispatch, SetStateAction, useCallback, useEffect, useState } from 'react';
import { TreeNode, Tree } from './FileTreeView.types';
import { findNestedNode } from './FileTreeView.utils';

interface Props {
  tree?: Tree;
  setOpenedFile: Dispatch<SetStateAction<TreeNode>>;
}

const renderTree = (node: TreeNode) => {
  return (
    <TreeItem key={node.name} itemId={node.id ?? node.name} label={node.name}>
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

  const handleItemClick = (_event: React.SyntheticEvent, itemId: string) => {
    openFile(itemId);
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
      // First, check if this is a single-file project (has a .scala file at root level)
      const rootScalaFiles = tree.filter(
        file => typeof file.content === 'string' && file.name.endsWith('.scala')
      );
      
      if (rootScalaFiles.length === 1) {
        // Single-file project: open the single .scala file
        if (rootScalaFiles[0].id !== undefined) {
          setMainNodeId(rootScalaFiles[0].id);
        }
        return;
      }
      
      // Multi-file project: look for Main.scala
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
        <SimpleTreeView
          aria-label="file tree view"
          slots={{
            endIcon: InsertDriveFileOutlined,
            collapseIcon: FolderOpenTwoTone,
            expandIcon: Folder,
          }}
          defaultExpandedItems={uniqueIds}
          defaultSelectedItems={mainNodeId}
          onItemClick={handleItemClick}
          sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: '100%' }}
        >
          {parsedTree.map((node: TreeNode, index: number) => (
            <div key={index}>{renderTree(node)}</div>
          ))}
        </SimpleTreeView>
      )}
    </>
  );
};
