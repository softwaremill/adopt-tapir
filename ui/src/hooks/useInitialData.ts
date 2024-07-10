import { useState } from 'react';
import { adjectives, animals, uniqueNamesGenerator } from 'unique-names-generator';
import {
  Builder,
  EffectImplementation,
  StackType,
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
    stack: StackType.Ox,
    implementation: EffectImplementation.Netty,
    addDocumentation: false,
    addMetrics: false,
    json: JSONImplementation.No,
    scalaVersion: ScalaVersion.Scala3,
    builder: Builder.ScalaCli,
  };
};
