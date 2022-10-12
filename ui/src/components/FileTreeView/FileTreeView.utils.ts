import { TreeNode, Tree } from './FileTreeView.types';

export const DEFAULT_NODE: TreeNode = { content: '', name: '', type: 'file' };

export const simulateMainNodeClick = (mainNodeId: string): void => {
  // simulate clicking on Main.scala
  // "selected" prop on TreeView doesn't work as expected
  const element = document.querySelector(`[id$="${mainNodeId}"]`);
  if (element instanceof HTMLElement && element.children[0] instanceof HTMLElement) {
    element.children[0].click();
  }
};

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
