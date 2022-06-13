import * as yup from 'yup';
import { EffectType, EffectImplementation, JSONImplementation } from 'api/starter';
import type { FormSelectOption } from '../FormSelect';
import type { FormRadioOption } from '../FormRadioGroup';

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

export const starterValidationSchema = yup
  .object({
    projectName: yup
      .string()
      .strict()
      .matches(/^[a-z0-9]+$/, 'Project name can consists of only lowercase characters and numbers')
      .required('This field is required'),
    groupId: yup
      .string()
      .strict()
      .lowercase('GroupId should follow Java package naming convention')
      .max(256, 'GroupId should be smaller than 256 characters')
      .required('This field is required'),
    tapirVersion: yup.string().required('This field is required'),
    effect: yup
      .mixed()
      .oneOf(
        Object.values(EffectType),
        `Effect type must be one of the following values: ${Object.keys(EffectType).join(', ')}`
      )
      .required('This field is required'),
    implementation: yup
      .mixed()
      .oneOf(
        Object.values(EffectImplementation),
        /* eslint-disable no-template-curly-in-string */
        'Server implementation must be one of the following values: ${values}'
      )
      .required('This field is required'),
    addDocumentation: yup.boolean().required('This field is required'),
    json: yup.mixed().oneOf(Object.values(JSONImplementation)).required('This field is required'),
  })
  .required();
