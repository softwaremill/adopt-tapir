import { EffectType, EffectImplementation, JSONImplementation } from 'api/starter';
import { EFFECT_IMPLEMENTATIONS_OPTIONS, JSON_OUTPUT_OPTIONS } from './ConfigurationForm.consts';
import type { FormSelectOption } from '../FormSelect';
import type { FormRadioOption } from '../FormRadioGroup';

/**
 * Effect type to effect implementation mapping
 */

const effectTypeImplementationMap: Record<EffectType, EffectImplementation[]> = {
  [EffectType.Future]: [EffectImplementation.Akka, EffectImplementation.Netty],
  [EffectType.IO]: [EffectImplementation.Http4s, EffectImplementation.Netty],
  [EffectType.ZIO]: [EffectImplementation.Http4s, EffectImplementation.ZIOHttp],
};

export const mapEffectTypeToEffectImplementation = (effectType: EffectType): EffectImplementation[] => {
  return effectTypeImplementationMap[effectType];
};

export const getEffectImplementationOptions = (effectType: EffectType): FormSelectOption[] => {
  const availableEffectImplementations = mapEffectTypeToEffectImplementation(effectType);

  return EFFECT_IMPLEMENTATIONS_OPTIONS.filter(({ value }) => availableEffectImplementations.includes(value));
};

/**
 * Effect type to json implementation mapping
 */

const commonJSONImplementations: JSONImplementation[] = [
  JSONImplementation.No,
  JSONImplementation.Circe,
  JSONImplementation.Jsoniter,
];

const effectTypeJsonImplementationMap: Record<EffectType, JSONImplementation[]> = {
  [EffectType.Future]: commonJSONImplementations,
  [EffectType.IO]: commonJSONImplementations,
  [EffectType.ZIO]: commonJSONImplementations.concat(JSONImplementation.ZIOJson),
};

export const mapEffectTypeToJSONImplementation = (effectType: EffectType): JSONImplementation[] => {
  return effectTypeJsonImplementationMap[effectType];
};

export const getJSONImplementationOptions = (effectType?: EffectType): FormRadioOption[] => {
  if (effectType) {
    const availableJSONImplementations = mapEffectTypeToJSONImplementation(effectType);

    return JSON_OUTPUT_OPTIONS.filter(({ value }) => availableJSONImplementations.includes(value));
  } else {
    return JSON_OUTPUT_OPTIONS.filter(({ value }) => value !== JSONImplementation.ZIOJson);
  }
};
