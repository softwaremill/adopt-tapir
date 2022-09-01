import {saveAs} from "file-saver";
import {FileTree} from "../components/FileTreeView";

export enum EffectType {
  Future = 'FutureEffect',
  IO = 'IOEffect',
  ZIO = 'ZIOEffect',
}

export enum EffectImplementation {
  Akka = 'Akka',
  Netty = 'Netty',
  Http4s = 'Http4s',
  ZIOHttp = 'ZIOHttp',
}

export enum JSONImplementation {
  Circe = 'Circe',
  Jsoniter = 'Jsoniter',
  ZIOJson = 'ZIOJson',
  No = 'No',
}

export enum ScalaVersion {
  Scala2 = 'Scala2',
  Scala3 = 'Scala3',
}

export enum Builder {
  Sbt = 'Sbt',
  ScalaCli = 'ScalaCli',
}

export type StarterRequest = {
  projectName: string;
  groupId: string;
  effect: EffectType;
  implementation: EffectImplementation;
  addDocumentation: boolean;
  addMetrics: boolean;
  json: JSONImplementation;
  scalaVersion: ScalaVersion;
  builder: Builder;
};

export async function doRequestStarter(formData: StarterRequest) {
  const serverAddress = !process.env.REACT_APP_SERVER_ADDRESS
    ? "https://adopt-tapir.softwaremill.com"
    : process.env.REACT_APP_SERVER_ADDRESS;
  const response = await fetch(`${serverAddress}/api/v1/starter.zip`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(formData),
  });

  if (!response.ok) {
    const json = await response.json();

    throw new Error(json.error || 'Something went wrong, please try again later.');
  }

  const blob = await response.blob();
  const filename = response.headers.get('Content-Disposition')?.split('filename=')[1].replaceAll('"', '');

  // download starter zip file
  saveAs(blob, filename ?? 'starter.zip');
}

// TODO use dedicated endpoint.
// Current request only simulates call to api. It will be replaced when endpoint is ready.
export async function doRequestPreview(formData: StarterRequest, consumer: (resp: FileTree) => void) {
  const serverAddress = !process.env.REACT_APP_SERVER_ADDRESS
    ? "https://adopt-tapir.softwaremill.com"
    : process.env.REACT_APP_SERVER_ADDRESS;
  const response = await fetch(`${serverAddress}/api/v1/starter.zip`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(formData),
  });

  if (!response.ok) {
    const json = await response.json();

    throw new Error(json.error || 'Something went wrong, please try again later.');
  } else {
    consumer(example);
  }
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
