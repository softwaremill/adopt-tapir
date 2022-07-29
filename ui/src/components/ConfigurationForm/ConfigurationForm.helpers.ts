import { EffectImplementation, EffectType, JSONImplementation, ScalaVersion } from 'api/starter';
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

export const getEffectImplementationOptions = (effectType: EffectType, scalaVer: ScalaVersion): FormSelectOption[] => {
  const availableEffectImplementations = mapEffectTypeToEffectImplementation(effectType);
  const forbiddenScala3Implementations = [EffectImplementation.Akka];

  return EFFECT_IMPLEMENTATIONS_OPTIONS.filter(({ value }) => availableEffectImplementations.includes(value)).filter(
    ({ value }) => scalaVer === ScalaVersion.Scala2 || !forbiddenScala3Implementations.includes(value)
  );
};

/**
 * Effect implementation to metrics supported
 */

export const isAddMetricsSupported = (effectImplementation: EffectImplementation): boolean => {
  return Boolean(effectImplementation) && effectImplementation !== EffectImplementation.Netty;
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
