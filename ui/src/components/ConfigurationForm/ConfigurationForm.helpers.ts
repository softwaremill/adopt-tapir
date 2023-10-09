import { EffectImplementation, EffectType, JSONImplementation, ScalaVersion } from 'api/starter';
import { EFFECT_IMPLEMENTATIONS_OPTIONS, JSON_OUTPUT_OPTIONS } from './ConfigurationForm.consts';
import type { FormSelectOption } from '../FormSelect';
import type { FormRadioOption } from '../FormRadioGroup';

/**
 * Effect type to effect implementation mapping
 */

const effectTypeImplementationMap: Record<EffectType, EffectImplementation[]> = {
  [EffectType.Future]: [EffectImplementation.Netty, EffectImplementation.VertX, EffectImplementation.Pekko],
  [EffectType.IO]: [EffectImplementation.Http4s, EffectImplementation.Netty, EffectImplementation.VertX],
  [EffectType.ZIO]: [
    EffectImplementation.Netty,
    EffectImplementation.Http4s,
    EffectImplementation.ZIOHttp,
    EffectImplementation.VertX,
  ],
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

const versionTypeJsonImlpementationMap: Record<ScalaVersion, JSONImplementation[]> = {
  [ScalaVersion.Scala2]: commonJSONImplementations,
  [ScalaVersion.Scala3]: commonJSONImplementations.concat(JSONImplementation.Pickler),
};

export const mapEffectTypeToJSONImplementation = (effectType: EffectType): JSONImplementation[] => {
  return effectTypeJsonImplementationMap[effectType];
};

export const mapScalaVersionToJSONImplementation = (scalaVersion: ScalaVersion): JSONImplementation[] => {
  return versionTypeJsonImlpementationMap[scalaVersion];
};

const getJSONImplementations = (scalaVersion?: ScalaVersion, effectType?: EffectType): JSONImplementation[] => {
  const implementationsForVersion = scalaVersion ? mapScalaVersionToJSONImplementation(scalaVersion) : [];
  const implementationsForEffect = effectType ? mapEffectTypeToJSONImplementation(effectType) : [];

  // Merge and deduplicate
  return [...new Set([...implementationsForVersion, ...implementationsForEffect])];
};

export const getJSONImplementationOptions = (
  scalaVersion?: ScalaVersion,
  effectType?: EffectType
): FormRadioOption[] => {
  const availableJSONImplementations = getJSONImplementations(scalaVersion, effectType) || commonJSONImplementations;

  return JSON_OUTPUT_OPTIONS.filter(({ value }) => availableJSONImplementations.includes(value));
};
