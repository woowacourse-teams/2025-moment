import { useSyncExternalStore } from 'react';

type StoreSubscriber = () => void;

export interface Store<TState> {
  getState: () => TState;
  setState: (value: TState | ((prev: TState) => TState)) => void;
  subscribe: (callback: StoreSubscriber) => () => void;
}

export function createStore<TState>(initialState: TState): Store<TState> {
  let state = initialState;
  const listeners = new Set<StoreSubscriber>();

  return {
    getState: () => state,
    setState: value => {
      const newState =
        typeof value === 'function' ? (value as (prev: TState) => TState)(state) : value;
      if (newState !== state) {
        state = newState;
        listeners.forEach(listener => listener());
      }
    },
    subscribe: callback => {
      listeners.add(callback);
      return () => listeners.delete(callback);
    },
  };
}

export function useStore<TState>(store: Store<TState>): TState {
  return useSyncExternalStore(store.subscribe, store.getState, store.getState);
}
