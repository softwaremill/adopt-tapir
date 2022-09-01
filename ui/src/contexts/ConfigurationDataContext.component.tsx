import { createContext, PropsWithChildren, useReducer } from 'react';
import { StarterRequest } from '../api/starter';
import { ContextAction, reducer } from './ConfigurationDataContext.actions';

export type ContextState = {
  formData?: StarterRequest;
};

const initialValue: [ContextState, (action: ContextAction) => void] = [{}, () => undefined];
export const ConfigurationDataContext = createContext(initialValue);

export function ConfigurationDataContextProvider({ children }: PropsWithChildren) {
  const [state, dispatch] = useReducer(reducer, {});
  return <ConfigurationDataContext.Provider value={[state, dispatch]}>{children}</ConfigurationDataContext.Provider>;
}
