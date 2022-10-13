import { EffectType, EffectImplementation, ScalaVersion, JSONImplementation } from 'api/starter';
import {
  getAvailableEffectImplementations,
  getEffectImplementationOptions,
  getJSONImplementationOptions,
} from '../ConfigurationForm.helpers';
import type { FormSelectOption } from '../../FormSelect';
import type { FormRadioOption } from '../../FormRadioGroup';

describe('configuration form helpers', () => {
  describe('.getAvailableEffectImplementations()', () => {
    const cases: [EffectType, ScalaVersion, EffectImplementation[]][] = [
      [EffectType.Future, ScalaVersion.Scala2, [EffectImplementation.Netty]],
      [EffectType.IO, ScalaVersion.Scala2, [EffectImplementation.Http4s, EffectImplementation.Netty]],
      [EffectType.ZIO, ScalaVersion.Scala2, [EffectImplementation.Http4s, EffectImplementation.ZIOHttp]],

      [EffectType.Future, ScalaVersion.Scala3, [EffectImplementation.Netty]],
      [EffectType.IO, ScalaVersion.Scala3, [EffectImplementation.Http4s, EffectImplementation.Netty]],
      [EffectType.ZIO, ScalaVersion.Scala3, [EffectImplementation.Http4s, EffectImplementation.ZIOHttp]],
    ];

    test.each(cases)(
      'should return set of effect implementations based on EffectType - (%s) and ScalaVersion - (%s)',
      (effectType, scalaVersion, effectImplementations) => {
        expect(getAvailableEffectImplementations(effectType, scalaVersion)).toEqual(effectImplementations);
      }
    );
  });

  describe('.getEffectImplementationOptions()', () => {
    const cases: [EffectType, ScalaVersion, FormSelectOption[]][] = [
      [
        EffectType.Future,
        ScalaVersion.Scala2,
        [
          {
            label: 'Netty',
            value: EffectImplementation.Netty,
          },
        ],
      ],
      [
        EffectType.IO,
        ScalaVersion.Scala2,
        [
          {
            label: 'http4s',
            value: EffectImplementation.Http4s,
          },
          {
            label: 'Netty',
            value: EffectImplementation.Netty,
          },
        ],
      ],
      [
        EffectType.ZIO,
        ScalaVersion.Scala2,
        [
          {
            label: 'http4s',
            value: EffectImplementation.Http4s,
          },
          {
            label: 'ZIO Http',
            value: EffectImplementation.ZIOHttp,
          },
        ],
      ],

      [
        EffectType.Future,
        ScalaVersion.Scala3,
        [
          {
            label: 'Netty',
            value: EffectImplementation.Netty,
          },
        ],
      ],
      [
        EffectType.IO,
        ScalaVersion.Scala3,
        [
          {
            label: 'http4s',
            value: EffectImplementation.Http4s,
          },
          {
            label: 'Netty',
            value: EffectImplementation.Netty,
          },
        ],
      ],
      [
        EffectType.ZIO,
        ScalaVersion.Scala3,
        [
          {
            label: 'http4s',
            value: EffectImplementation.Http4s,
          },
          {
            label: 'ZIO Http',
            value: EffectImplementation.ZIOHttp,
          },
        ],
      ],
    ];

    test.each(cases)(
      'should return set of form select options based on EffectType - (%s) and ScalaVersion - (%s)',
      (effectType, scalaVersion, formSelectOptions) => {
        const effectImplementationOptions = getEffectImplementationOptions(effectType, scalaVersion);

        expect(effectImplementationOptions).toHaveLength(formSelectOptions.length);
        expect(effectImplementationOptions).toEqual(expect.arrayContaining(formSelectOptions));
      }
    );
  });

  describe('.getJSONImplementationOptions()', () => {
    const cases: [EffectType, FormRadioOption[]][] = [
      [
        EffectType.Future,
        [
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
            label: 'jsoniter',
            value: JSONImplementation.Jsoniter,
          },
        ],
      ],
      [
        EffectType.IO,
        [
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
            label: 'jsoniter',
            value: JSONImplementation.Jsoniter,
          },
        ],
      ],
      [
        EffectType.ZIO,
        [
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
            label: 'jsoniter',
            value: JSONImplementation.Jsoniter,
          },
          {
            label: 'zio-json',
            value: JSONImplementation.ZIOJson,
          },
        ],
      ],
    ];

    test.each(cases)(
      'should return set of form radio options based on EffectType - (%s)',
      (effectType, formRadioOptions) => {
        const jsonImplementationOptions = getJSONImplementationOptions(effectType);

        expect(jsonImplementationOptions).toHaveLength(formRadioOptions.length);
        expect(jsonImplementationOptions).toEqual(expect.arrayContaining(formRadioOptions));
      }
    );
  });
});
