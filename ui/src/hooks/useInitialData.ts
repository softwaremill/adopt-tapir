import { useState } from 'react';
import { adjectives, animals, uniqueNamesGenerator } from 'unique-names-generator';
import {
  Builder,
  EffectImplementation,
  EffectType,
  JSONImplementation,
  ScalaVersion,
  StarterRequest,
} from '../api/starter';

export const useInitialData: () => StarterRequest = () => {
  const [name] = useState(
    uniqueNamesGenerator({
      dictionaries: [adjectives, animals],
      length: 2,
      separator: '-',
    })
  );

  return {
    projectName: name,
    groupId: 'com.softwaremill',
    effect: EffectType.IO,
    implementation: EffectImplementation.Http4s,
    addDocumentation: false,
    addMetrics: false,
    json: JSONImplementation.No,
    scalaVersion: ScalaVersion.Scala3,
    builder: Builder.Sbt,
  };
};
