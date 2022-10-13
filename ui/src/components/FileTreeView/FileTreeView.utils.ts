import { TreeNode, Tree } from './FileTreeView.types';

export const DEFAULT_NODE: TreeNode = { content: '', name: '', type: 'file' };

export const findNestedNode = (tree: Tree, nodeId: string): TreeNode => {
  let foundNode: TreeNode = DEFAULT_NODE;
  JSON.stringify(tree, (_, nestedNode) => {
    if (nestedNode && nestedNode.id === nodeId) {
      foundNode = nestedNode;
    }
    return nestedNode;
  });
  return foundNode;
};
