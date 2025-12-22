import { EffectImplementation, StackType, JSONImplementation, ScalaVersion } from '@/api/starter';
import {
  getAvailableEffectImplementations,
  getEffectImplementationOptions,
  getJSONImplementationOptions,
} from '../ConfigurationForm.helpers';
import type { FormSelectOption } from '../../FormSelect';
import type { FormRadioOption } from '../../FormRadioGroup';

describe('configuration form helpers', () => {
  describe('.getAvailableEffectImplementations()', () => {
    const cases: [StackType, ScalaVersion, EffectImplementation[]][] = [
      [
        StackType.Future,
        ScalaVersion.Scala2,
        [EffectImplementation.Netty, EffectImplementation.VertX, EffectImplementation.Pekko],
      ],
      [
        StackType.IO,
        ScalaVersion.Scala2,
        [EffectImplementation.Http4s, EffectImplementation.Netty, EffectImplementation.VertX],
      ],
      [
        StackType.ZIO,
        ScalaVersion.Scala2,
        [
          EffectImplementation.Netty,
          EffectImplementation.Http4s,
          EffectImplementation.ZIOHttp,
          EffectImplementation.VertX,
        ],
      ],

      [
        StackType.Future,
        ScalaVersion.Scala3,
        [EffectImplementation.Netty, EffectImplementation.VertX, EffectImplementation.Pekko],
      ],
      [
        StackType.IO,
        ScalaVersion.Scala3,
        [EffectImplementation.Http4s, EffectImplementation.Netty, EffectImplementation.VertX],
      ],
      [StackType.Ox, ScalaVersion.Scala3, [EffectImplementation.Netty]],
      [
        StackType.ZIO,
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
      'should return set of effect implementations based on StackType - (%s) and ScalaVersion - (%s)',
      (stackType, scalaVersion, effectImplementations) => {
        expect(getAvailableEffectImplementations(stackType)).toEqual(effectImplementations);
      }
    );
  });

  describe('.getEffectImplementationOptions()', () => {
    const cases: [StackType, ScalaVersion, FormSelectOption[]][] = [
      [
        StackType.Future,
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
        StackType.IO,
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
        StackType.ZIO,
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
        StackType.Future,
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
        StackType.IO,
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
        StackType.Ox,
        ScalaVersion.Scala3,
        [
          {
            label: 'Netty',
            value: EffectImplementation.Netty,
          },
        ],
      ],
      [
        StackType.ZIO,
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
      'should return set of form select options based on StackType - (%s) and ScalaVersion - (%s)',
      (stackType, scalaVersion, formSelectOptions) => {
        const effectImplementationOptions = getEffectImplementationOptions(stackType);

        expect(effectImplementationOptions).toHaveLength(formSelectOptions.length);
        expect(effectImplementationOptions).toEqual(expect.arrayContaining(formSelectOptions));
      }
    );
  });

  describe('.getJSONImplementationOptions()', () => {
    const cases: [ScalaVersion, StackType, FormRadioOption[]][] = [
      // Scala 2 variants
      [
        ScalaVersion.Scala2,
        StackType.Future,
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
        StackType.IO,
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
        StackType.ZIO,
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
        StackType.Future,
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
        StackType.IO,
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
        StackType.Ox,
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
        StackType.ZIO,
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
      'should return set of form radio options based on StackType - (%s)',
      (scalaVersion, stackType, formRadioOptions) => {
        const jsonImplementationOptions = getJSONImplementationOptions(scalaVersion, stackType);

        expect(jsonImplementationOptions).toHaveLength(formRadioOptions.length);
        expect(jsonImplementationOptions).toEqual(expect.arrayContaining(formRadioOptions));
      }
    );
  });
});
