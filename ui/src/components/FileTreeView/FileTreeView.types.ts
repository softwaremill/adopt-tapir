export type TreeNode = {
  content: string | TreeNode;
  name: string;
  type: 'file' | 'directory';
  id?: string;
};

export type Tree = TreeNode[];
