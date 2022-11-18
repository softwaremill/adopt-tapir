import { EffectImplementation, EffectType, JSONImplementation } from 'api/starter';
import { EFFECT_IMPLEMENTATIONS_OPTIONS, JSON_OUTPUT_OPTIONS } from './ConfigurationForm.consts';
import type { FormSelectOption } from '../FormSelect';
import type { FormRadioOption } from '../FormRadioGroup';

/**
 * Effect type to effect implementation mapping
 */

const effectTypeImplementationMap: Record<EffectType, EffectImplementation[]> = {
  [EffectType.Future]: [EffectImplementation.Netty, EffectImplementation.Vertx],
  [EffectType.IO]: [EffectImplementation.Http4s, EffectImplementation.Netty, EffectImplementation.Vertx],
  [EffectType.ZIO]: [EffectImplementation.Http4s, EffectImplementation.ZIOHttp, EffectImplementation.Vertx],
};

const mapEffectTypeToEffectImplementation = (effectType: EffectType): EffectImplementation[] => {
  return effectTypeImplementationMap[effectType];
};

export const getAvailableEffectImplementations = (effectType: EffectType): EffectImplementation[] => {
  const availableEffectImplementations = mapEffectTypeToEffectImplementation(effectType);
  return availableEffectImplementations === undefined ? [] : availableEffectImplementations;
};

export const getEffectImplementationOptions = (effectType: EffectType): FormSelectOption[] => {
  const effectImplementations = getAvailableEffectImplementations(effectType);

  return EFFECT_IMPLEMENTATIONS_OPTIONS.filter(({ value }) => effectImplementations.includes(value));
};

/**
 * Effect type to json implementation mapping
 */

const commonJSONImplementations: JSONImplementation[] = [
  JSONImplementation.No,
  JSONImplementation.Circe,
  JSONImplementation.UPickle,
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
