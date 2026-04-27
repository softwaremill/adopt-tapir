import { useEffect, useState } from 'react';
import { Light as SyntaxHighlighterBase, SyntaxHighlighterProps } from 'react-syntax-highlighter';
import scala from 'react-syntax-highlighter/dist/esm/languages/hljs/scala';
import markdown from 'react-syntax-highlighter/dist/esm/languages/hljs/markdown';
import ini from 'react-syntax-highlighter/dist/esm/languages/hljs/ini';
import plaintext from 'react-syntax-highlighter/dist/esm/languages/hljs/plaintext';
import { a11yLight } from 'react-syntax-highlighter/dist/esm/styles/hljs';
import { IconButton, Tooltip } from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import { TreeNode } from '../FileTreeView/FileTreeView.types';
import { useStyles } from './FileContentView.styles';

type SupportedLanguage = 'scala' | 'markdown' | 'ini' | 'plaintext';

// Type assertion to fix React 18 compatibility issue with class-based component types
const SyntaxHighlighter = SyntaxHighlighterBase as unknown as React.FC<SyntaxHighlighterProps>;

SyntaxHighlighterBase.registerLanguage('scala', scala);
SyntaxHighlighterBase.registerLanguage('markdown', markdown);
SyntaxHighlighterBase.registerLanguage('ini', ini);
SyntaxHighlighterBase.registerLanguage('plaintext', plaintext);

type Props = {
  openedFile: TreeNode;
};

export function FileContentView({ openedFile }: Props) {
  const { classes } = useStyles();
  const [language, setLanguage] = useState<SupportedLanguage>('plaintext');
  const [copied, setCopied] = useState(false);

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

  useEffect(() => {
    setCopied(false);
  }, [openedFile.name, openedFile.content]);

  useEffect(() => {
    if (!copied) {
      return;
    }
    const timeout = setTimeout(() => setCopied(false), 2000);
    return () => clearTimeout(timeout);
  }, [copied]);

  const fileContent = String(openedFile.content);
  const hasContent = fileContent.length > 0;

  const handleCopy = async () => {
    await navigator.clipboard.writeText(fileContent);
    setCopied(true);
  };

  return (
    <div className={classes.wrapper}>
      <Tooltip title={copied ? 'Copied!' : 'Copy file contents'} arrow>
        <span className={classes.copyButton}>
          <IconButton
            color="secondary"
            size="small"
            aria-label="copy file contents"
            onClick={handleCopy}
            disabled={!hasContent}
          >
            <ContentCopyIcon fontSize="small" />
          </IconButton>
        </span>
      </Tooltip>
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
        {fileContent}
      </SyntaxHighlighter>
    </div>
  );
}
