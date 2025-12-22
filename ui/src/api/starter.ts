import { saveAs } from 'file-saver';
import { Tree } from '@/components/FileTreeView/FileTreeView.types';

export enum StackType {
  Future = 'FutureStack',
  IO = 'IOStack',
  ZIO = 'ZIOStack',
  Ox = 'OxStack',
}

export enum EffectImplementation {
  Netty = 'Netty',
  Http4s = 'Http4s',
  ZIOHttp = 'ZIOHttp',
  VertX = 'VertX',
  Pekko = 'Pekko',
}

export enum JSONImplementation {
  Circe = 'Circe',
  UPickle = 'UPickle',
  Pickler = 'Pickler',
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
  stack: StackType;
  implementation: EffectImplementation;
  addDocumentation: boolean;
  addMetrics: boolean;
  json: JSONImplementation;
  scalaVersion: ScalaVersion;
  builder: Builder;
};

export const serverAddress = import.meta.env.VITE_SERVER_ADDRESS ?? 'https://adopt-tapir.softwaremill.com';

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

export async function doRequestPreview(formData: StarterRequest, consumer: (resp: Tree) => void) {
  const serverAddress = import.meta.env.VITE_SERVER_ADDRESS || 'https://adopt-tapir.softwaremill.com';
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
