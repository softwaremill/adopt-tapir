import { StarterRequest } from '../api/starter';
import { ContextState } from './ConfigurationDataContext.component';

export type ContextAction = Clear | Set;

type Clear = {
  type: 'reset';
};

type Set = {
  type: 'set';
  formData: StarterRequest;
};

export function reducer(state: ContextState, action: ContextAction) {
  switch (action.type) {
    case 'reset':
      return {};
    case 'set':
      return { formData: action.formData };
    default:
      throw Error(`Unknown action: ${JSON.stringify(action)} dispatched to ConfigurationDataContext.`);
  }
}

export function setFormData(formData: StarterRequest): ContextAction {
  return {
    type: 'set',
    formData,
  };
}

const doReset: Clear = { type: 'reset' };

export function resetFormData(): ContextAction {
  return doReset;
}
