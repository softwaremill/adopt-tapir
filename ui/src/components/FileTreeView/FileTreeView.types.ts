import { NodeAbsoluteLocation } from './FileTreeView.utils';

export type FileTree = FileTreeNode[];

export type FileTreeNode = FileNode | DirNode;

export type FileNode = {
  name: string;
  type: 'file';
  content: string;
};

export type DirNode = {
  name: string;
  type: 'directory';
  children: FileTreeNode[];
};

export type TreeState = {
  openedDirs: NodeAbsoluteLocation[];
  toggleDir: (loc: NodeAbsoluteLocation) => void;
  isDirOpened: (loc: NodeAbsoluteLocation) => boolean;
  openedFile: NodeAbsoluteLocation;
  openFile: (loc: NodeAbsoluteLocation) => void;
};
