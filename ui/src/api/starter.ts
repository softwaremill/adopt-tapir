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

export type StarterRequest = {
  projectName: string;
  groupId: string;
  effect: EffectType;
  implementation: EffectImplementation;
  tapirVersion: string;
  addDocumentation: boolean;
  addMetrics: boolean;
  json: JSONImplementation;
};
