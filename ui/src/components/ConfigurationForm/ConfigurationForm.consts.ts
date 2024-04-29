import * as yup from 'yup';
import {
  Builder,
  EffectImplementation,
  EffectType,
  JSONImplementation,
  ScalaVersion,
  StarterRequest,
} from 'api/starter';
import type { FormSelectOption } from '../FormSelect';
import type { FormRadioOption } from '../FormRadioGroup';
import {
  getAvailableEffectImplementations,
  getEffectImplementationOptions,
  mapEffectTypeToJSONImplementation,
  mapScalaVersionToJSONImplementation,
} from './ConfigurationForm.helpers';

export const SCALA_VERSION_OPTIONS: FormSelectOption<string>[] = [
  {
    label: '2',
    value: ScalaVersion.Scala2,
  },
  {
    label: '3',
    value: ScalaVersion.Scala3,
  },
];

export const EFFECT_TYPE_OPTIONS: FormSelectOption<EffectType>[] = [
  {
    label: 'Future',
    value: EffectType.Future,
  },
  {
    label: 'IO (cats-effect)',
    value: EffectType.IO,
  },
  {
    label: 'Sync (direct-style)',
    value: EffectType.Sync,
  },
  {
    label: 'ZIO',
    value: EffectType.ZIO,
  },
];

export const EFFECT_IMPLEMENTATIONS_OPTIONS: FormSelectOption<EffectImplementation>[] = [
  {
    label: 'Netty',
    value: EffectImplementation.Netty,
  },
  {
    label: 'http4s',
    value: EffectImplementation.Http4s,
  },
  {
    label: 'ZIO Http',
    value: EffectImplementation.ZIOHttp,
  },
  {
    label: 'Vert.X',
    value: EffectImplementation.VertX,
  },
  {
    label: 'Pekko HTTP',
    value: EffectImplementation.Pekko,
  },
];

export const ENDPOINTS_OPTIONS: FormRadioOption<boolean>[] = [
  {
    label: 'yes',
    value: true,
  },
  {
    label: 'no',
    value: false,
  },
];

export const JSON_OUTPUT_OPTIONS: FormRadioOption<JSONImplementation>[] = [
  {
    label: "don't add",
    value: JSONImplementation.No,
  },
  {
    label: 'circe',
    value: JSONImplementation.Circe,
  },
  {
    label: 'µPickle',
    value: JSONImplementation.UPickle,
  },
  {
    label: 'pickler',
    value: JSONImplementation.Pickler,
  },
  {
    label: 'jsoniter',
    value: JSONImplementation.Jsoniter,
  },
  {
    label: 'zio-json',
    value: JSONImplementation.ZIOJson,
  },
];

export const BUILDER_OPTIONS: FormRadioOption<Builder>[] = [
  {
    label: 'sbt',
    value: Builder.Sbt,
  },
  {
    label: 'Scala CLI',
    value: Builder.ScalaCli,
  },
];

const labelGetter = (
  option: FormSelectOption | FormRadioOption
): FormSelectOption['label'] | FormRadioOption['label'] => option.label;

const valueGetter = (
  option: FormSelectOption | FormRadioOption
): FormSelectOption['value'] | FormRadioOption['value'] => option.value;

const getEffectImplementationFieldMessage = (effectType: EffectType): string =>
  `Server implementation must be one of the following values: ${getEffectImplementationOptions(effectType)
    .map(labelGetter)
    .join(', ')}`;

const REQUIRED_FIELD_MESSAGE = 'This field is required';

export const starterValidationSchema = yup
  .object({
    projectName: yup
      .string()
      .strict()
      .matches(
        /^[a-z0-9_]$|^[a-z0-9_]+[a-z0-9_-]*[a-z0-9_]+$/,
        'Project name can consists of only lowercase characters, numbers, underscores and dashes (not at the beginning or end).'
      )
      .required(REQUIRED_FIELD_MESSAGE),
    groupId: yup
      .string()
      .strict()
      .matches(
        /(?:^[a-z][a-z0-9_]*|[a-z][a-z0-9_]*\.[a-z0-9_]+)+$/,
        'Group ID should follow Java package naming convention'
      )
      .max(256, 'Group ID length should be smaller than 256 characters')
      .required(REQUIRED_FIELD_MESSAGE),
    effect: yup
      .mixed()
      .oneOf(
        EFFECT_TYPE_OPTIONS.map(valueGetter),
        `Effect type must be one of the following values: ${EFFECT_TYPE_OPTIONS.map(labelGetter).join(', ')}`
      )
      .required(REQUIRED_FIELD_MESSAGE),
    scalaVersion: yup
      .mixed()
      .oneOf(
        SCALA_VERSION_OPTIONS.map(valueGetter),
        `Scala version must be one of the following values: ${SCALA_VERSION_OPTIONS.map(labelGetter).join(', ')}`
      )
      .required(REQUIRED_FIELD_MESSAGE),
    implementation: yup
      .mixed()
      .test('effect implementation validation', (value: EffectImplementation, context) => {
        const { effect } = context.parent as StarterRequest;

        const effectImplementations = getAvailableEffectImplementations(effect);

        return (
          effectImplementations.includes(value) ||
          context.createError({ message: getEffectImplementationFieldMessage(effect) })
        );
      })
      .required(REQUIRED_FIELD_MESSAGE),
    addDocumentation: yup.boolean().required(REQUIRED_FIELD_MESSAGE),
    json: yup
      .mixed()
      .when('effect', {
        is: (effect: EffectType) => effect !== EffectType.ZIO,
        // NOTE: any effect type besides ZIO will work here as an argument
        then: schema => schema.oneOf(mapEffectTypeToJSONImplementation(EffectType.Future)),
        otherwise: schema => schema.oneOf(mapEffectTypeToJSONImplementation(EffectType.ZIO)),
      })
      .when('scalaVersion', {
        is: (scalaVersion: ScalaVersion) => scalaVersion !== ScalaVersion.Scala3,
        then: schema => schema.oneOf(mapScalaVersionToJSONImplementation(ScalaVersion.Scala2)),
        otherwise: schema => schema.oneOf(mapScalaVersionToJSONImplementation(ScalaVersion.Scala3)),
      })
      .required(REQUIRED_FIELD_MESSAGE),
    addMetrics: yup.boolean().required(REQUIRED_FIELD_MESSAGE),
    builder: yup
      .mixed()
      .oneOf(
        BUILDER_OPTIONS.map(valueGetter),
        `Builder must be one of the following values: ${BUILDER_OPTIONS.map(labelGetter).join(', ')}`
      )
      .required(REQUIRED_FIELD_MESSAGE),
  })
  .required();
