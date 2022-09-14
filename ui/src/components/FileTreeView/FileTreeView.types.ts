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
  content: FileTreeNode[];
};

export type TreeState = {
  openedDirs: NodeAbsoluteLocation[];
  toggleDir: (loc: NodeAbsoluteLocation) => void;
  isDirOpened: (loc: NodeAbsoluteLocation) => boolean;
  openedFile: NodeAbsoluteLocation;
  openFile: (loc: NodeAbsoluteLocation) => void;
};

export function getAllDirectories(prefix: string[], files: FileTreeNode[]): string[][] {
  const dirs: string[][] = [];
  files
    .filter(isDirectory)
    .map(file => file as DirNode)
    .forEach(dir => {
      const newPrefix = [...prefix, dir.name];
      dirs.push(newPrefix);
      dirs.push(...getAllDirectories(newPrefix, dir.content));
    });
  return dirs;
}

const isDirectory = (node: FileTreeNode): boolean => {
  return node.type === 'directory';
};
