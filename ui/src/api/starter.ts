export enum EffectType {
  Future = 'FutureEffect',
  'cats-effect' = 'IOEffect',
  ZIO = 'ZIOEffect',
}

export enum EffectImplementation {
  Akka = 'Akka',
  Netty = 'Netty',
  Http4s = 'Http4s',
  ZIOHttp = 'ZIOHttp',
}

export type StarterRequest = {
  projectName: string;
  groupId: string;
  effect: EffectType;
  implementation: EffectImplementation;
  tapirVersion: string;
  addDocumentation: boolean;
};
