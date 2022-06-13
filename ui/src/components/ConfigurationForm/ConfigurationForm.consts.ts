import * as yup from 'yup';
import { EffectType, EffectImplementation, JSONImplementation } from 'api/starter';
import type { FormSelectOption } from '../FormSelect';
import type { FormRadioOption } from '../FormRadioGroup';
import {
  mapEffectTypeToEffectImplementation,
  mapEffectTypeToJSONImplementation,
  getEffectImplementationOptions,
} from './ConfigurationForm.helpers';

export const TAPIR_VERSION_OPTIONS: FormSelectOption<string>[] = [
  {
    label: '1.0.0',
    value: '1.0.0-RC2',
  },
];

export const EFFECT_TYPE_OPTIONS: FormSelectOption<EffectType>[] = [
  {
    label: 'Future',
    value: EffectType.Future,
  },
  {
    label: 'cats-effect',
    value: EffectType.IO,
  },
  {
    label: 'ZIO',
    value: EffectType.ZIO,
  },
];

export const EFFECT_IMPLEMENTATIONS_OPTIONS: FormSelectOption<EffectImplementation>[] = [
  {
    label: 'Akka HTTP',
    value: EffectImplementation.Akka,
  },
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
    label: 'jsoniter',
    value: JSONImplementation.Jsoniter,
  },
  {
    label: 'zio-json',
    value: JSONImplementation.ZIOJson,
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
      .matches(/^[a-z0-9]+$/, 'Project name can consists of only lowercase characters and numbers')
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
    tapirVersion: yup.string().required(REQUIRED_FIELD_MESSAGE),
    effect: yup
      .mixed()
      .oneOf(
        EFFECT_TYPE_OPTIONS.map(valueGetter),
        `Effect type must be one of the following values: ${EFFECT_TYPE_OPTIONS.map(labelGetter).join(', ')}`
      )
      .required(REQUIRED_FIELD_MESSAGE),
    // NOTE: unfortunately this is the only way of multiple .when cases in yup :shrug-emoji:
    implementation: yup
      .mixed()
      .when('effect', {
        is: EffectType.Future,
        then: schema =>
          schema.oneOf(
            mapEffectTypeToEffectImplementation(EffectType.Future),
            getEffectImplementationFieldMessage(EffectType.Future)
          ),
        otherwise: schema =>
          schema.when('effect', {
            is: EffectType.IO,
            then: schema =>
              schema.oneOf(
                mapEffectTypeToEffectImplementation(EffectType.IO),
                getEffectImplementationFieldMessage(EffectType.IO)
              ),
            otherwise: schema =>
              schema.when('effect', {
                is: EffectType.ZIO,
                then: schema =>
                  schema.oneOf(
                    mapEffectTypeToEffectImplementation(EffectType.ZIO),
                    getEffectImplementationFieldMessage(EffectType.ZIO)
                  ),
              }),
          }),
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
      .required(REQUIRED_FIELD_MESSAGE),
  })
  .required();
