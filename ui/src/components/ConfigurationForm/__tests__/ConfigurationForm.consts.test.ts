import * as yup from 'yup';
import {
  Builder,
  EffectImplementation,
  StackType,
  JSONImplementation,
  ScalaVersion,
  StarterRequest,
} from 'api/starter';
import { starterValidationSchema } from '../ConfigurationForm.consts';

const TEST_FORM_VALUES: StarterRequest = {
  projectName: 'test-project',
  groupId: 'softwaremill.com',
  stack: StackType.Future,
  scalaVersion: ScalaVersion.Scala3,
  implementation: EffectImplementation.Netty,
  addDocumentation: false,
  json: JSONImplementation.No,
  addMetrics: false,
  builder: Builder.Sbt,
};

describe('configuration consts', () => {
  describe('starterValidationSchema', () => {
    it('should return true, if form values conforms the validation schema', () => {
      expect(starterValidationSchema.isValidSync(TEST_FORM_VALUES)).toBe(true);
    });

    it('should return false, if any form value does not conforms the validation schema', () => {
      const formValues = {
        ...TEST_FORM_VALUES,
        stack: 'test stack',
      };

      expect(starterValidationSchema.isValidSync(formValues)).toBe(false);
    });

    it('should return false, if form values are empty', () => {
      const formValues = {};

      expect(starterValidationSchema.isValidSync(formValues)).toBe(false);
    });

    describe('projectName field', () => {
      const projectNameSchema = yup.reach(starterValidationSchema, 'projectName');
      const cases: { projectName: string | undefined; expected: boolean }[] = [
        { projectName: 'test-project', expected: true },
        { projectName: 'test_project_longer', expected: true },
        { projectName: 'test-project123_numbers_456', expected: true },
        { projectName: '_test-project', expected: true },
        { projectName: 'test-project_', expected: true },
        { projectName: 'test_123_project', expected: true },
        { projectName: 'test project', expected: false },
        { projectName: 'TestProject', expected: false },
        { projectName: '-test-project', expected: false },
        { projectName: 'test-project-', expected: false },
        { projectName: 'test@#$@%@$@project', expected: false },
        { projectName: '', expected: false },
        { projectName: undefined, expected: false },
      ];

      test.each(cases)(
        'should return $expected based on project name [$projectName] validity',
        ({ projectName, expected }) => {
          expect(projectNameSchema.isValidSync(projectName)).toBe(expected);
        }
      );
    });

    describe('groupId field', () => {
      const groupIdSchema = yup.reach(starterValidationSchema, 'groupId');
      const cases: { groupId: string | undefined; expected: boolean }[] = [
        { groupId: 'softwaremill.com', expected: true },
        { groupId: 'com.softwaremill', expected: true },
        { groupId: 'com.softwaremill.v2', expected: true },
        { groupId: 'com.softwaremill.deep.nested.v1', expected: true },
        { groupId: 'com.SoftwaremiLL', expected: false },
        { groupId: 'com.software@mill$%^', expected: false },
        { groupId: Array.from({ length: 257 }).fill('').join(''), expected: false },
        { groupId: '', expected: false },
        { groupId: undefined, expected: false },
      ];

      test.each(cases)('should return $expected based on group id [$groupId] validity', ({ groupId, expected }) => {
        expect(groupIdSchema.isValidSync(groupId)).toBe(expected);
      });
    });

    describe('stack field', () => {
      const stackSchema = yup.reach(starterValidationSchema, 'stack');
      const cases: { stackType: StackType | undefined; expected: boolean }[] = [
        { stackType: StackType.Future, expected: true },
        { stackType: StackType.IO, expected: true },
        { stackType: StackType.ZIO, expected: true },
        { stackType: StackType.Ox, expected: true },
        { stackType: 'notValidEffect' as StackType, expected: false },
        { stackType: '' as StackType, expected: false },
        { stackType: undefined, expected: false },
      ];

      test.each(cases)(
        'should return $expected based on effect type [$stackType] validity',
        ({ stackType, expected }) => {
          expect(stackSchema.isValidSync(stackType)).toBe(expected);
        }
      );
    });

    describe('scalaVersion field', () => {
      const scalaVersionSchema = yup.reach(starterValidationSchema, 'scalaVersion');
      const cases: { scalaVersion: ScalaVersion | undefined; expected: boolean }[] = [
        { scalaVersion: ScalaVersion.Scala2, expected: true },
        { scalaVersion: ScalaVersion.Scala3, expected: true },
        { scalaVersion: 'notScalaVersion' as ScalaVersion, expected: false },
        { scalaVersion: '' as ScalaVersion, expected: false },
        { scalaVersion: undefined, expected: false },
      ];

      test.each(cases)(
        'should return $expected based on scala version [$scalaVersion] validity',
        ({ scalaVersion, expected }) => {
          expect(scalaVersionSchema.isValidSync(scalaVersion)).toBe(expected);
        }
      );
    });

    describe('implementation field', () => {
      const effectImplementationSchema = yup.object({
        stack: yup.reach(starterValidationSchema, 'stack'),
        scalaVersion: yup.reach(starterValidationSchema, 'scalaVersion'),
        implementation: yup.reach(starterValidationSchema, 'implementation'),
      });

      // NOTE: it's easier to comprehend cases definitions this way, step with casesDetails is purerly due formatting issues
      const casesDetails: [StackType, ScalaVersion, EffectImplementation, boolean][] = [
        // Scala 2 variants
        [StackType.Future, ScalaVersion.Scala2, EffectImplementation.Netty, true],
        [StackType.Future, ScalaVersion.Scala2, EffectImplementation.Http4s, false],
        [StackType.Future, ScalaVersion.Scala2, EffectImplementation.ZIOHttp, false],
        [StackType.Future, ScalaVersion.Scala2, EffectImplementation.Pekko, true],

        [StackType.IO, ScalaVersion.Scala2, EffectImplementation.Netty, true],
        [StackType.IO, ScalaVersion.Scala2, EffectImplementation.Http4s, true],
        [StackType.IO, ScalaVersion.Scala2, EffectImplementation.ZIOHttp, false],

        [StackType.ZIO, ScalaVersion.Scala2, EffectImplementation.Netty, true],
        [StackType.ZIO, ScalaVersion.Scala2, EffectImplementation.Http4s, true],
        [StackType.ZIO, ScalaVersion.Scala2, EffectImplementation.ZIOHttp, true],

        // Scala 3 variants
        [StackType.Future, ScalaVersion.Scala3, EffectImplementation.Netty, true],
        [StackType.Future, ScalaVersion.Scala3, EffectImplementation.Http4s, false],
        [StackType.Future, ScalaVersion.Scala3, EffectImplementation.ZIOHttp, false],
        [StackType.Future, ScalaVersion.Scala3, EffectImplementation.Pekko, true],

        [StackType.IO, ScalaVersion.Scala3, EffectImplementation.Netty, true],
        [StackType.IO, ScalaVersion.Scala3, EffectImplementation.Http4s, true],
        [StackType.IO, ScalaVersion.Scala3, EffectImplementation.ZIOHttp, false],

        [StackType.Ox, ScalaVersion.Scala3, EffectImplementation.Netty, true],
        [StackType.Ox, ScalaVersion.Scala3, EffectImplementation.Http4s, false],
        [StackType.Ox, ScalaVersion.Scala3, EffectImplementation.ZIOHttp, false],
        [StackType.Ox, ScalaVersion.Scala3, EffectImplementation.Pekko, false],

        [StackType.ZIO, ScalaVersion.Scala3, EffectImplementation.Netty, true],
        [StackType.ZIO, ScalaVersion.Scala3, EffectImplementation.Http4s, true],
        [StackType.ZIO, ScalaVersion.Scala3, EffectImplementation.ZIOHttp, true],
      ];

      const cases = casesDetails.map(([stackType, scalaVersion, effectImplementation, expected]) => ({
        stackType,
        scalaVersion,
        effectImplementation,
        expected,
      }));

      test.each(cases)(
        'should return $expected based on effect type [$stackType], scala version [$scalaVersion] and effect implementation [$effectImplementation] combination validity',
        ({ stackType, scalaVersion, effectImplementation, expected }) => {
          expect(
            effectImplementationSchema.isValidSync({
              stack: stackType,
              scalaVersion,
              implementation: effectImplementation,
            })
          ).toBe(expected);
        }
      );
    });

    describe('addDocumentation field', () => {
      const addDocumentationSchema = yup.reach(starterValidationSchema, 'addDocumentation');
      const cases: { addDocumentation: boolean | undefined; expected: boolean }[] = [
        { addDocumentation: true, expected: true },
        { addDocumentation: false, expected: true },
        { addDocumentation: undefined, expected: false },
      ];

      test.each(cases)(
        'should return $expected based on add documentation [$addDocumentation] validity',
        ({ addDocumentation, expected }) => {
          expect(addDocumentationSchema.isValidSync(addDocumentation)).toBe(expected);
        }
      );
    });

    describe('json field', () => {
      const jsonSchema = yup.object({
        stack: yup.reach(starterValidationSchema, 'stack'),
        json: yup.reach(starterValidationSchema, 'json'),
      });

      const casesDetails: [ScalaVersion, StackType, JSONImplementation, boolean][] = [
        // Scala 2 variants
        [ScalaVersion.Scala2, StackType.Future, JSONImplementation.No, true],
        [ScalaVersion.Scala2, StackType.Future, JSONImplementation.Circe, true],
        [ScalaVersion.Scala2, StackType.Future, JSONImplementation.UPickle, true],
        [ScalaVersion.Scala2, StackType.Future, JSONImplementation.Jsoniter, true],
        [ScalaVersion.Scala2, StackType.Future, JSONImplementation.ZIOJson, false],
        [ScalaVersion.Scala2, StackType.Future, JSONImplementation.Pickler, false],

        [ScalaVersion.Scala2, StackType.IO, JSONImplementation.No, true],
        [ScalaVersion.Scala2, StackType.IO, JSONImplementation.Circe, true],
        [ScalaVersion.Scala2, StackType.IO, JSONImplementation.UPickle, true],
        [ScalaVersion.Scala2, StackType.IO, JSONImplementation.Jsoniter, true],
        [ScalaVersion.Scala2, StackType.IO, JSONImplementation.ZIOJson, false],
        [ScalaVersion.Scala2, StackType.IO, JSONImplementation.Pickler, false],

        [ScalaVersion.Scala2, StackType.ZIO, JSONImplementation.No, true],
        [ScalaVersion.Scala2, StackType.ZIO, JSONImplementation.Circe, true],
        [ScalaVersion.Scala2, StackType.ZIO, JSONImplementation.UPickle, true],
        [ScalaVersion.Scala2, StackType.ZIO, JSONImplementation.Jsoniter, true],
        [ScalaVersion.Scala2, StackType.ZIO, JSONImplementation.ZIOJson, true],
        [ScalaVersion.Scala2, StackType.ZIO, JSONImplementation.Pickler, false],

        // Scala 3 variants
        [ScalaVersion.Scala3, StackType.Future, JSONImplementation.No, true],
        [ScalaVersion.Scala3, StackType.Future, JSONImplementation.Circe, true],
        [ScalaVersion.Scala3, StackType.Future, JSONImplementation.UPickle, true],
        [ScalaVersion.Scala3, StackType.Future, JSONImplementation.Jsoniter, true],
        [ScalaVersion.Scala3, StackType.Future, JSONImplementation.ZIOJson, false],
        [ScalaVersion.Scala3, StackType.Future, JSONImplementation.Pickler, true],

        [ScalaVersion.Scala3, StackType.IO, JSONImplementation.No, true],
        [ScalaVersion.Scala3, StackType.IO, JSONImplementation.Circe, true],
        [ScalaVersion.Scala3, StackType.IO, JSONImplementation.UPickle, true],
        [ScalaVersion.Scala3, StackType.IO, JSONImplementation.Jsoniter, true],
        [ScalaVersion.Scala3, StackType.IO, JSONImplementation.ZIOJson, false],
        [ScalaVersion.Scala3, StackType.IO, JSONImplementation.Pickler, true],

        [ScalaVersion.Scala3, StackType.Ox, JSONImplementation.No, true],
        [ScalaVersion.Scala3, StackType.Ox, JSONImplementation.Circe, true],
        [ScalaVersion.Scala3, StackType.Ox, JSONImplementation.UPickle, true],
        [ScalaVersion.Scala3, StackType.Ox, JSONImplementation.Jsoniter, true],
        [ScalaVersion.Scala3, StackType.Ox, JSONImplementation.ZIOJson, false],
        [ScalaVersion.Scala3, StackType.Ox, JSONImplementation.Pickler, true],

        [ScalaVersion.Scala3, StackType.ZIO, JSONImplementation.No, true],
        [ScalaVersion.Scala3, StackType.ZIO, JSONImplementation.Circe, true],
        [ScalaVersion.Scala3, StackType.ZIO, JSONImplementation.UPickle, true],
        [ScalaVersion.Scala3, StackType.ZIO, JSONImplementation.Jsoniter, true],
        [ScalaVersion.Scala3, StackType.ZIO, JSONImplementation.ZIOJson, true],
        [ScalaVersion.Scala3, StackType.ZIO, JSONImplementation.Pickler, true],
      ];

      const cases = casesDetails.map(([scalaVersion, stackType, jsonImplementation, expected]) => ({
        stackType,
        scalaVersion,
        jsonImplementation,
        expected,
      }));

      test.each(cases)(
        'should return $expected based on stack type [$stackType] and json implementation [$jsonImplementation] combination validity',
        ({ scalaVersion, stackType, jsonImplementation, expected }) => {
          expect(jsonSchema.isValidSync({ scalaVersion, stack: stackType, json: jsonImplementation })).toBe(expected);
        }
      );
    });

    describe('addMetrics field', () => {
      const addMetricsSchema = yup.reach(starterValidationSchema, 'addMetrics');
      const cases: { addMetrics: boolean | undefined; expected: boolean }[] = [
        { addMetrics: true, expected: true },
        { addMetrics: false, expected: true },
        { addMetrics: undefined, expected: false },
      ];

      test.each(cases)(
        'should return $expected based on add metrics [$addMetrics] validity',
        ({ addMetrics, expected }) => {
          expect(addMetricsSchema.isValidSync(addMetrics)).toBe(expected);
        }
      );
    });

    describe('builder field', () => {
      const builderSchema = yup.reach(starterValidationSchema, 'builder');
      const cases: { builder: Builder | undefined; expected: boolean }[] = [
        { builder: Builder.Sbt, expected: true },
        { builder: Builder.ScalaCli, expected: true },
        { builder: 'notScalaVersion' as Builder, expected: false },
        { builder: '' as Builder, expected: false },
        { builder: undefined, expected: false },
      ];

      test.each(cases)('should return $expected based on builder [$builder] validity', ({ builder, expected }) => {
        expect(builderSchema.isValidSync(builder)).toBe(expected);
      });
    });
  });
});
