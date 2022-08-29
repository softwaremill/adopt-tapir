import {useStyles} from "./FileContentView.styles";
import {Paper} from "@mui/material";
import {FileNode, FileTree} from "../FileTreeView/FileTreeView.types";
import {NodeAbsoluteLocation} from "../FileTreeView/FileTreeView.utils";
import {useEffect, useState} from "react";
import {Light as SyntaxHighlighter} from "react-syntax-highlighter";
import scala from 'react-syntax-highlighter/dist/esm/languages/hljs/scala';
import markdown from 'react-syntax-highlighter/dist/esm/languages/hljs/markdown';
import ini from 'react-syntax-highlighter/dist/esm/languages/hljs/ini';
import plaintext from 'react-syntax-highlighter/dist/esm/languages/hljs/plaintext';
import { defaultStyle } from 'react-syntax-highlighter/dist/esm/styles/hljs';

type SupportedLanguage = 'scala' | 'markdown' | 'ini' | 'plaintext'

SyntaxHighlighter.registerLanguage('scala', scala);
SyntaxHighlighter.registerLanguage('markdown', markdown);
SyntaxHighlighter.registerLanguage('ini', ini);
SyntaxHighlighter.registerLanguage('plaintext', plaintext);

type Props = {
  files: FileTree,
  opened: NodeAbsoluteLocation
}

export function FileContentView({files, opened}: Props) {
  const {classes} = useStyles()

  const [name, setName] = useState(opened.getName());
  const [content, setContent] = useState("");
  const [language, setLanguage] = useState<SupportedLanguage>('plaintext');
  useEffect(() => {
    setName(opened.getName());
    const slugs = opened.getSlugs();
    const findFile: (tree: FileTree, remaining: string[]) => FileTree = (tree, remaining) => {
      const current = remaining[0];
      const found = tree.find(f => f.name === current);
      if (found === undefined) {
        throw Error(`File '${remaining}' not found in ${JSON.stringify(tree)}`);
      } else if (found.type === 'directory') {
        return findFile(found.children, remaining.slice(1))
      } else if (found.type === 'file') {
        return [found];
      } else {
        throw Error(`File '${remaining}' not found in ${JSON.stringify(tree)}`);
      }
    }
    setContent((findFile(files, slugs)[0] as FileNode).content)
  }, [opened, files]);
  useEffect(() => {
    if (name === undefined) {
      return;
    }
    const format = name.split('.').at(-1);
    switch (format) {
      case 'scala':
      case 'sc':
      case 'sbt':
        setLanguage('scala');
        break;
      case 'md':
        setLanguage('markdown');
        break
      case 'conf':
        setLanguage('ini');
        break;
      default:
        setLanguage('plaintext')
        break;
    }
  }, [name]);
  return (<>
    <Paper className={classes.wrapper} variant="outlined">
      <SyntaxHighlighter language={language} showLineNumbers={true} style={defaultStyle}>
        {content}
      </SyntaxHighlighter>
    </Paper>
    </>);
}
