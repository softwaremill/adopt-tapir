import { EffectImplementation, StackType, JSONImplementation, ScalaVersion } from 'api/starter';
import { EFFECT_IMPLEMENTATIONS_OPTIONS, STACK_TYPE_OPTIONS, JSON_OUTPUT_OPTIONS } from './ConfigurationForm.consts';
import type { FormSelectOption } from '../FormSelect';
import type { FormRadioOption } from '../FormRadioGroup';

/**
 * Stack type to effect implementation mapping
 */

const stackTypeImplementationMap: Record<StackType, EffectImplementation[]> = {
  [StackType.Future]: [EffectImplementation.Netty, EffectImplementation.VertX, EffectImplementation.Pekko],
  [StackType.IO]: [EffectImplementation.Http4s, EffectImplementation.Netty, EffectImplementation.VertX],
  [StackType.Ox]: [EffectImplementation.Netty],
  [StackType.ZIO]: [
    EffectImplementation.Netty,
    EffectImplementation.Http4s,
    EffectImplementation.ZIOHttp,
    EffectImplementation.VertX,
  ],
};

const versionTypeEffectImplementationMap: Record<ScalaVersion, StackType[]> = {
  [ScalaVersion.Scala2]: [StackType.Future, StackType.IO, StackType.ZIO],
  [ScalaVersion.Scala3]: [StackType.Future, StackType.IO, StackType.Ox, StackType.ZIO],
};

const mapStackTypeToEffectImplementation = (stackType: StackType): EffectImplementation[] => {
  return stackTypeImplementationMap[stackType];
};

export const getAvailableEffectImplementations = (stackType: StackType): EffectImplementation[] => {
  const availableEffectImplementations = mapStackTypeToEffectImplementation(stackType);
  return availableEffectImplementations === undefined ? [] : availableEffectImplementations;
};

export const mapScalaVersionToStackType = (scalaVersion: ScalaVersion): StackType[] => {
  return versionTypeEffectImplementationMap[scalaVersion];
};

const getStackTypes = (scalaVersion?: ScalaVersion): StackType[] => {
  return scalaVersion ? mapScalaVersionToStackType(scalaVersion) : Object.values(StackType);
};

export const getStackTypeOptions = (scalaVersion?: ScalaVersion): FormSelectOption[] => {
  const stackTypes = getStackTypes(scalaVersion);
  return STACK_TYPE_OPTIONS.filter(({ value }) => stackTypes.includes(value));
};

export const getEffectImplementationOptions = (stackType: StackType): FormSelectOption[] => {
  const effectImplementations = getAvailableEffectImplementations(stackType);

  return EFFECT_IMPLEMENTATIONS_OPTIONS.filter(({ value }) => effectImplementations.includes(value));
};

/**
 * Stack type to json implementation mapping
 */

const commonJSONImplementations: JSONImplementation[] = [
  JSONImplementation.No,
  JSONImplementation.Circe,
  JSONImplementation.UPickle,
  JSONImplementation.Jsoniter,
];

const stackTypeJsonImplementationMap: Record<StackType, JSONImplementation[]> = {
  [StackType.Future]: commonJSONImplementations,
  [StackType.IO]: commonJSONImplementations,
  [StackType.Ox]: commonJSONImplementations,
  [StackType.ZIO]: commonJSONImplementations.concat(JSONImplementation.ZIOJson),
};

const versionTypeJsonImlpementationMap: Record<ScalaVersion, JSONImplementation[]> = {
  [ScalaVersion.Scala2]: commonJSONImplementations,
  [ScalaVersion.Scala3]: commonJSONImplementations.concat(JSONImplementation.Pickler),
};

export const mapStackTypeToJSONImplementation = (stackType: StackType): JSONImplementation[] => {
  return stackTypeJsonImplementationMap[stackType];
};

export const mapScalaVersionToJSONImplementation = (scalaVersion: ScalaVersion): JSONImplementation[] => {
  return versionTypeJsonImlpementationMap[scalaVersion];
};

const getJSONImplementations = (scalaVersion?: ScalaVersion, stackType?: StackType): JSONImplementation[] => {
  const implementationsForVersion = scalaVersion ? mapScalaVersionToJSONImplementation(scalaVersion) : [];
  const implementationsForEffect = stackType ? mapStackTypeToJSONImplementation(stackType) : [];

  // Merge and deduplicate
  return [...new Set([...implementationsForVersion, ...implementationsForEffect])];
};

export const getJSONImplementationOptions = (scalaVersion?: ScalaVersion, stackType?: StackType): FormRadioOption[] => {
  const availableJSONImplementations = getJSONImplementations(scalaVersion, stackType) || commonJSONImplementations;

  return JSON_OUTPUT_OPTIONS.filter(({ value }) => availableJSONImplementations.includes(value));
};
