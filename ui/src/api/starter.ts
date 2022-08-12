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
