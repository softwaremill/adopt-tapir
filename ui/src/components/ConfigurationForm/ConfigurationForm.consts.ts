import * as yup from 'yup';
import { EffectType, EffectImplementation } from 'api/starter';
import type { FormSelectOption } from '../FormSelect';
import type { FormRadioOption } from '../FormRadioGroup';

export const TAPIR_VERSION_OPTIONS: FormSelectOption[] = [
  {
    label: '1.0.0',
    value: '1.0.0-RC2',
  },
];

export const EFFECT_TYPE_OPTIONS: FormSelectOption[] = Object.entries(EffectType).map(([key, value]) => ({
  label: key,
  value: value,
}));

export const EFFECT_IMPLEMENTATION_OPTIONS: FormSelectOption[] = Object.entries(EffectImplementation).map(
  ([key, value]) => ({
    label: key,
    value: value,
  })
);

export const ENDPOINTS_OPTIONS: FormRadioOption[] = [
  {
    label: 'yes',
    value: true,
  },
  {
    label: 'no',
    value: false,
  },
];

export const JSON_INPUT_OUTPUT_OPTIONS: FormRadioOption[] = [
  {
    label: 'circe',
    value: 'circe',
  },
  {
    label: 'jsoniter',
    value: 'jsoniter',
  },
  {
    label: 'zio-json',
    value: 'zio-json',
  },
  {
    label: 'no',
    value: false, // check?
  },
];

export const configurationSchema = yup
  .object({
    projectName: yup
      .string()
      .strict()
      .lowercase('Project name should be in lowercase')
      .required('This field is required'),
    groupId: yup
      .string()
      .strict()
      .lowercase('GroupId should follow Java package naming convention')
      .max(256, 'GroupId should be smaller than 256 characters')
      .required('This field is required'),
    tapirVersion: yup.string().required('This field is required'),
    // scalaVersion: yup.string().required(),
    effect: yup.mixed().oneOf(Object.values(EffectType)).required('This field is required'),
    implementation: yup.mixed().oneOf(Object.values(EffectImplementation)).required('This field is required'),
    addDocumentation: yup.boolean().required('This field is required'),
    // metricEndpoint: yup.boolean().required(),
    // jsonInputOutput: yup.mixed(),
  })
  .required();
