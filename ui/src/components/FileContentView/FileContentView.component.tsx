import { useEffect, useState } from 'react';
import { Light as SyntaxHighlighter } from 'react-syntax-highlighter';
import scala from 'react-syntax-highlighter/dist/esm/languages/hljs/scala';
import markdown from 'react-syntax-highlighter/dist/esm/languages/hljs/markdown';
import ini from 'react-syntax-highlighter/dist/esm/languages/hljs/ini';
import plaintext from 'react-syntax-highlighter/dist/esm/languages/hljs/plaintext';
import { a11yLight } from 'react-syntax-highlighter/dist/esm/styles/hljs';
import { TreeNode } from '../FileTreeView/FileTreeView.types';

type SupportedLanguage = 'scala' | 'markdown' | 'ini' | 'plaintext';

SyntaxHighlighter.registerLanguage('scala', scala);
SyntaxHighlighter.registerLanguage('markdown', markdown);
SyntaxHighlighter.registerLanguage('ini', ini);
SyntaxHighlighter.registerLanguage('plaintext', plaintext);

type Props = {
  openedFile: TreeNode;
};

export function FileContentView({ openedFile }: Props) {
  const [language, setLanguage] = useState<SupportedLanguage>('plaintext');

  useEffect(() => {
    if (openedFile.name === undefined) {
      return;
    }
    const format = openedFile.name.split('.').at(-1);
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
  }, [openedFile.name]);

  return (
    <>
      {/* @ts-expect-error - react-syntax-highlighter type definitions are incompatible with TypeScript 5.x */}
      <SyntaxHighlighter
        customStyle={{
          margin: 0,
          backgroundColor: 'rgb(255, 255, 255)',
          padding: 0,
          height: '100%',
        }}
        lineNumberContainerStyle={{
          float: 'left',
          textAlign: 'right',
          paddingRight: '1em',
          minWidth: '44px',
          userSelect: 'none',
          WebkitUserSelect: 'none',
        }}
        language={language}
        showLineNumbers={true}
        showInlineLineNumbers={false}
        style={a11yLight}
      >
        {String(openedFile.content)}
      </SyntaxHighlighter>
    </>
  );
}
