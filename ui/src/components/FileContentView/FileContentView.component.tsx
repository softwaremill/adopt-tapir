import { FileNode, FileTree, NodeAbsoluteLocation } from '../FileTreeView';
import { useEffect, useState } from 'react';
import { Light as SyntaxHighlighter } from 'react-syntax-highlighter';
import scala from 'react-syntax-highlighter/dist/esm/languages/hljs/scala';
import markdown from 'react-syntax-highlighter/dist/esm/languages/hljs/markdown';
import ini from 'react-syntax-highlighter/dist/esm/languages/hljs/ini';
import plaintext from 'react-syntax-highlighter/dist/esm/languages/hljs/plaintext';
import { a11yLight } from 'react-syntax-highlighter/dist/esm/styles/hljs';

type SupportedLanguage = 'scala' | 'markdown' | 'ini' | 'plaintext';

SyntaxHighlighter.registerLanguage('scala', scala);
SyntaxHighlighter.registerLanguage('markdown', markdown);
SyntaxHighlighter.registerLanguage('ini', ini);
SyntaxHighlighter.registerLanguage('plaintext', plaintext);

type Props = {
  files?: FileTree;
  opened: NodeAbsoluteLocation;
};

export function FileContentView({ files, opened }: Props) {
  const [name, setName] = useState(opened.getName());
  const [content, setContent] = useState('');
  const [language, setLanguage] = useState<SupportedLanguage>('plaintext');
  useEffect(() => {
    if (!files) {
      return;
    }
    setName(opened.getName());
    const slugs = opened.getSlugs();
    const findFile: (tree: FileTree, remaining: string[]) => FileTree = (tree, remaining) => {
      const current = remaining[0];
      const found = tree.find(f => f.name === current);
      if (found === undefined) {
        throw Error(`File '${remaining}' not found in ${JSON.stringify(tree)}`);
      } else if (found.type === 'directory') {
        return findFile(found.content, remaining.slice(1));
      } else if (found.type === 'file') {
        return [found];
      } else {
        throw Error(`File '${remaining}' not found in ${JSON.stringify(tree)}`);
      }
    };
    setContent((findFile(files, slugs)[0] as FileNode).content);
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
        break;
      case 'conf':
        setLanguage('ini');
        break;
      default:
        setLanguage('plaintext');
        break;
    }
  }, [name]);
  return (
    <>
      <SyntaxHighlighter
        customStyle={{
          margin: 0,
          backgroundColor: 'rgb(255, 255, 255)',
          padding: 0,
          height: '100%',
          overflowY: 'auto',
        }}
        lineNumberStyle={{ minWidth: '44px' }}
        language={language}
        showLineNumbers={true}
        style={a11yLight}
      >
        {content}
      </SyntaxHighlighter>
    </>
  );
}
