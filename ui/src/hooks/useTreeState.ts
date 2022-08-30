import {TreeState} from "../components/FileTreeView/FileTreeView.types";
import {Reducer, useCallback, useReducer, useState} from "react";
import {NodeAbsoluteLocation} from "../components/FileTreeView/FileTreeView.utils";

export const useTreeState: (opened: string) => TreeState = (opened: string) => {
  const [openedDirs, toggleDir] = useReducer<Reducer<NodeAbsoluteLocation[], NodeAbsoluteLocation>>(
    (dirs, touchedDir) => {
      let updated = [];
      let processed = false;
      for (let i = 0; i < dirs.length; i++) {
        const current = dirs[i];
        // If it is opened, we open its parent.
        // If child of touched dir is opened, we close it, and open parent of touched dir.
        if (touchedDir.isSameAs(current) || touchedDir.isParentOf(current)) {
          updated.push(touchedDir.getParent());
          processed = true;
          // If parent of touched dir we replace it with touched.
        } else if (touchedDir.isChildOf(current)) {
          updated.push(touchedDir);
          processed = true;
        } else {
          updated.push(current)
        }
      }

      if (!processed) {
        updated.push(touchedDir)
      }

      return updated.filter(d => !d.isRoot());
    },
    []);
  const isDirOpened = useCallback(
    (dir: NodeAbsoluteLocation) => {
      if (dir.isRoot()) {
        return true;
      }
      return openedDirs.find(openedDir => dir.isSameAs(openedDir) || dir.isParentOf(openedDir)) !== undefined;
    },
    [openedDirs]);
  const [openedFile, openFile] = useState<NodeAbsoluteLocation>(new NodeAbsoluteLocation(opened));
  return {openedDirs, toggleDir, isDirOpened, openedFile, openFile};
}
