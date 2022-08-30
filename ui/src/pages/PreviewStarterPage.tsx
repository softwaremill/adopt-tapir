import {Box, Button, Grid} from "@mui/material";
import {useStyles} from "../App.styles";
import {FileTreeView} from "../components/FileTreeView/FileTreeView.component";
import {FileTree, TreeState} from "../components/FileTreeView/FileTreeView.types";
import {Reducer, useCallback, useReducer, useState} from "react";
import {NodeAbsoluteLocation, RootNodeLocation} from "../components/FileTreeView/FileTreeView.utils";
import {FileContentView} from "../components/FileContentView/FileContentView.component";

const useTreeState: (opened: string) => TreeState = (opened: string) => {
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

export function PreviewStarterPage() {
  const {classes, cx} = useStyles();
  const [files] = useState(example);
  const treeState = useTreeState("README.md");

  // 92.5px is the height of buttons panel.
  return (<Grid container style={{height: 'calc(100% - 92.5px)'}}>
        <Grid item xs={3} className={classes.fullHeight}>
          <Box className={cx(classes.fullHeight, classes.treeViewContainer)}>
              <FileTreeView tree={files} location={RootNodeLocation} state={treeState}/>
          </Box>
        </Grid>
        <Grid item xs={9} className={classes.fullHeight}>
          <Box className={cx(classes.fullHeight, classes.fileViewContainer)}>
              <FileContentView files={files} opened={treeState.openedFile}/>
          </Box>
        </Grid>
        <Grid item xs={12} className={classes.fileViewButtonsContainer}>
            <Button
              variant="contained"
              color="secondary"
              size="medium"
              disableElevation>
              Back
            </Button>
            <Button
              variant="contained"
              color="primary"
              size="medium"
              type="submit"
              disableElevation>GENERATE .ZIP</Button>
        </Grid>
  </Grid>);
}

const example: FileTree = [
  {
    name: 'src',
    type: 'directory',
    children: [
      {
        name: 'main',
        type: 'directory',
        children: [
          {
            name: 'scala',
            type: 'directory',
            children: [
              {
                name: 'com',
                type: 'directory',
                children: [
                  {
                    name: 'sml',
                    type: 'directory',
                    children: [
                      {
                        name: 'Endpoints.scala',
                        type: 'file',
                        content: 'content of Endpoints.scala'
                      },
                      {
                        name: 'Main.scala',
                        type: 'file',
                        content: 'package test\n' +
                        'import sttp.tapir.server.netty.{NettyFutureServer, NettyFutureServerOptions}\n\n' +
                        'import scala.concurrent.duration.Duration\n' +
                        'import scala.concurrent.{Await, ExecutionContext, Future}\n' +
                        'import scala.io.StdIn\n' +
                        'import ExecutionContext.Implicits.global\n\n' +
                        '@main def run(): Unit =\n\n' +
                        '    val serverOptions = NettyFutureServerOptions.customiseInterceptors\n' +
                        '        .metricsInterceptor(Endpoints.prometheusMetrics.metricsInterceptor())\n' +
                        '        .options\n\n' +
                        '        val port = sys.env.get(\"http.port\").map(_.toInt).getOrElse(8080)\n' +
                        '        val program =\n' +
                        '          for\n' +
                        '            binding <- NettyFutureServer(serverOptions).port(port).addEndpoints(Endpoints.all).start()\n' +
                        '            _ <- Future {\n' +
                        '                println(s\"Go to http://localhost:\$\{binding.port\}/docs to open SwaggerUI. Press ENTER key to exit.\")\n' +
                        '                StdIn.readLine()\n' +
                        '                }\n' +
                        '            stop <- binding.stop()\n' +
                        '          yield stop\n' +
                        '        Await.result(program, Duration.Inf)'
                      }
                    ]
                  },
                ]
              },
            ]
          },
        ]
      },
      {
        name: 'test',
        type: 'directory',
        children: [
          {
            name: 'scala',
            type: 'directory',
            children: [
              {
                name: 'com',
                type: 'directory',
                children: [
                  {
                    name: 'sml',
                    type: 'directory',
                    children: [
                      {
                        name: 'Endpoints.scala',
                        type: 'file',
                        content: 'content of Endpoints.scala'
                      },
                      {
                        name: 'Main.scala',
                        type: 'file',
                        content: 'content of Main.scala'
                      }
                    ]
                  },
                ]
              },
            ]
          },
        ]
      }
    ]
  },
  {
    name: '.scalafmt.conf',
    type: 'file',
    content: 'version = 3.5.8\n' +
      'maxColumn = 140\n' +
      'runner.dialect = scala3'
  },
  {
    name: 'build.sc',
    type: 'file',
    content: 'content of build.sc'
  },
  {
    name: 'README.md',
    type: 'file',
    content: '## Quick start\n' +
      '\n' +
      'If you don\'t have scala-cli install yet, please follow these [installation instructions](https://scala-cli.virtuslab.org/install).\n' +
      'You can use the following commands to compile, test and run the projet:\n' +
      '\n' +
      '```shell\n' +
      'scala-cli compile --test . # build the project (\'--test\' means that tests will be also compiled)\n' +
      'scala-cli test . # run the tests\n' +
      'scala-cli run . # run the application (Main)\n' +
      'scala-cli fmt --check . # run scalaformat check on all scala files and print summary, removing \'--check\' fixes misformatted files\n' +
      '```\n' +
      '\n' +
      'Alternatively, you can use scala-clie via a docker image:\n' +
      '\n' +
      '```shell\n' +
      'docker run -ti --rm -v $(pwd):/app virtuslab/scala-cli compile --test /app # build the project (\'--test\' means that tests will be also compiled)\n' +
      'docker run -ti --rm -v $(pwd):/app virtuslab/scala-cli test /app # run the tests\n' +
      'docker run -ti --rm -p \'8080:8080\' -v $(pwd):/app virtuslab/scala-cli run /app # run the application (Main)\n' +
      '```\n' +
      '\n' +
      'For more details check the [scala-cli commands](https://scala-cli.virtuslab.org/docs/commands/basics) page.\n' +
      '\n' +
      '## Links:\n' +
      '\n' +
      '* [tapir documentation](https://tapir.softwaremill.com/en/latest/)\n' +
      '* [tapir github](https://github.com/softwaremill/tapir)\n' +
      '* [bootzooka: template microservice using tapir](https://softwaremill.github.io/bootzooka/)\n' +
      '* [scala-cli](ttps://scala-cli.virtuslab.org)'
  }
];
