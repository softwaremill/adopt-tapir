import { EffectImplementation, EffectType, JSONImplementation, ScalaVersion } from 'api/starter';
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
      [
        EffectType.Future,
        ScalaVersion.Scala2,
        [EffectImplementation.Netty, EffectImplementation.VertX, EffectImplementation.Pekko],
      ],
      [
        EffectType.IO,
        ScalaVersion.Scala2,
        [EffectImplementation.Http4s, EffectImplementation.Netty, EffectImplementation.VertX],
      ],
      [
        EffectType.ZIO,
        ScalaVersion.Scala2,
        [
          EffectImplementation.Netty,
          EffectImplementation.Http4s,
          EffectImplementation.ZIOHttp,
          EffectImplementation.VertX,
        ],
      ],

      [
        EffectType.Future,
        ScalaVersion.Scala3,
        [EffectImplementation.Netty, EffectImplementation.VertX, EffectImplementation.Pekko],
      ],
      [
        EffectType.IO,
        ScalaVersion.Scala3,
        [EffectImplementation.Http4s, EffectImplementation.Netty, EffectImplementation.VertX],
      ],
      [
        EffectType.ZIO,
        ScalaVersion.Scala3,
        [
          EffectImplementation.Netty,
          EffectImplementation.Http4s,
          EffectImplementation.ZIOHttp,
          EffectImplementation.VertX,
        ],
      ],
    ];

    test.each(cases)(
      'should return set of effect implementations based on EffectType - (%s) and ScalaVersion - (%s)',
      (effectType, scalaVersion, effectImplementations) => {
        expect(getAvailableEffectImplementations(effectType)).toEqual(effectImplementations);
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
          {
            label: 'Vert.X',
            value: EffectImplementation.VertX,
          },
          {
            label: 'Pekko HTTP',
            value: EffectImplementation.Pekko,
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
          {
            label: 'Vert.X',
            value: EffectImplementation.VertX,
          },
        ],
      ],
      [
        EffectType.ZIO,
        ScalaVersion.Scala2,
        [
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
          {
            label: 'Vert.X',
            value: EffectImplementation.VertX,
          },
          {
            label: 'Pekko HTTP',
            value: EffectImplementation.Pekko,
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
          {
            label: 'Vert.X',
            value: EffectImplementation.VertX,
          },
        ],
      ],
      [
        EffectType.ZIO,
        ScalaVersion.Scala3,
        [
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
        ],
      ],
    ];

    test.each(cases)(
      'should return set of form select options based on EffectType - (%s) and ScalaVersion - (%s)',
      (effectType, scalaVersion, formSelectOptions) => {
        const effectImplementationOptions = getEffectImplementationOptions(effectType);

        expect(effectImplementationOptions).toHaveLength(formSelectOptions.length);
        expect(effectImplementationOptions).toEqual(expect.arrayContaining(formSelectOptions));
      }
    );
  });

  describe('.getJSONImplementationOptions()', () => {
    const cases: [ScalaVersion, EffectType, FormRadioOption[]][] = [
      // Scala 2 variants
      [
        ScalaVersion.Scala2,
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
        ScalaVersion.Scala2,
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
        ScalaVersion.Scala2,
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

      // Scala 3 variants
      [
        ScalaVersion.Scala3,
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
            label: 'pickler',
            value: JSONImplementation.Pickler,
          },
          {
            label: 'jsoniter',
            value: JSONImplementation.Jsoniter,
          },
        ],
      ],
      [
        ScalaVersion.Scala3,
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
            label: 'pickler',
            value: JSONImplementation.Pickler,
          },
          {
            label: 'jsoniter',
            value: JSONImplementation.Jsoniter,
          },
        ],
      ],
      [
        ScalaVersion.Scala3,
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
        ],
      ],
    ];

    test.each(cases)(
      'should return set of form radio options based on EffectType - (%s)',
      (scalaVersion, effectType, formRadioOptions) => {
        const jsonImplementationOptions = getJSONImplementationOptions(scalaVersion, effectType);

        expect(jsonImplementationOptions).toHaveLength(formRadioOptions.length);
        expect(jsonImplementationOptions).toEqual(expect.arrayContaining(formRadioOptions));
      }
    );
  });
});
