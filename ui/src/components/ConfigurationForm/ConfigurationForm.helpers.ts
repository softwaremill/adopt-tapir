import { EffectType, EffectImplementation } from 'api/starter';
import type { FormSelectOption } from '../FormSelect';

const effectTypeImplementationMap: Record<EffectType, EffectImplementation[]> = {
  [EffectType.Future]: [EffectImplementation.Akka, EffectImplementation.Netty],
  [EffectType['cats-effect']]: [EffectImplementation.Http4s, EffectImplementation.Netty],
  [EffectType.ZIO]: [EffectImplementation.Http4s, EffectImplementation.ZIOHttp],
};

export const mapEffectTypeToEffectImplementation = (effectType: EffectType): EffectImplementation[] => {
  return effectTypeImplementationMap[effectType];
};

export const getEffectImplementationOptions = (effectType: EffectType): FormSelectOption[] => {
  const availableEffectImplementations = mapEffectTypeToEffectImplementation(effectType);

  return Object.entries(EffectImplementation)
    .filter(([, value]) => availableEffectImplementations.includes(value))
    .map(([key, value]) => ({
      label: key,
      value: value,
    }));
};
