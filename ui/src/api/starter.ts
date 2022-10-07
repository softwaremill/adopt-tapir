import { saveAs } from 'file-saver';
import { FileTree } from '../components/FileTreeView';

export enum EffectType {
  Future = 'FutureEffect',
  IO = 'IOEffect',
  ZIO = 'ZIOEffect',
}

export enum EffectImplementation {
  Netty = 'Netty',
  Http4s = 'Http4s',
  ZIOHttp = 'ZIOHttp',
}

export enum JSONImplementation {
  Circe = 'Circe',
  UPickle = 'UPickle',
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

export const serverAddress = process.env.REACT_APP_SERVER_ADDRESS ?? 'https://adopt-tapir.softwaremill.com';

export async function doRequestStarter(formData: StarterRequest) {
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

export async function doRequestPreview(formData: StarterRequest, consumer: (resp: FileTree) => void) {
  const serverAddress = !process.env.REACT_APP_SERVER_ADDRESS
    ? 'https://adopt-tapir.softwaremill.com'
    : process.env.REACT_APP_SERVER_ADDRESS;
  const response = await fetch(`${serverAddress}/api/v1/content`, {
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

  consumer(await response.json());
}
