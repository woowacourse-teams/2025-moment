import { renderHook, act } from '@testing-library/react';
import { createStore, useStore } from './core';

describe('createStore', () => {
  describe('getState', () => {
    it('초기 상태를 반환한다', () => {
      const store = createStore(0);
      expect(store.getState()).toBe(0);
    });

    it('객체 초기 상태를 반환한다', () => {
      const initial = { count: 0, name: 'test' };
      const store = createStore(initial);
      expect(store.getState()).toEqual(initial);
    });
  });

  describe('setState', () => {
    it('직접 값으로 상태를 업데이트한다', () => {
      const store = createStore(0);
      store.setState(5);
      expect(store.getState()).toBe(5);
    });

    it('업데이터 함수로 상태를 업데이트한다', () => {
      const store = createStore(10);
      store.setState(prev => prev + 5);
      expect(store.getState()).toBe(15);
    });

    it('같은 값으로 setState하면 리스너가 호출되지 않는다', () => {
      const store = createStore(0);
      const listener = jest.fn();
      store.subscribe(listener);
      store.setState(0);
      expect(listener).not.toHaveBeenCalled();
    });

    it('다른 값으로 setState하면 리스너가 호출된다', () => {
      const store = createStore(0);
      const listener = jest.fn();
      store.subscribe(listener);
      store.setState(1);
      expect(listener).toHaveBeenCalledTimes(1);
    });

    it('여러 리스너가 모두 호출된다', () => {
      const store = createStore('initial');
      const listener1 = jest.fn();
      const listener2 = jest.fn();
      store.subscribe(listener1);
      store.subscribe(listener2);
      store.setState('updated');
      expect(listener1).toHaveBeenCalledTimes(1);
      expect(listener2).toHaveBeenCalledTimes(1);
    });
  });

  describe('subscribe / unsubscribe', () => {
    it('구독 해제 후 리스너가 호출되지 않는다', () => {
      const store = createStore(0);
      const listener = jest.fn();
      const unsubscribe = store.subscribe(listener);
      unsubscribe();
      store.setState(1);
      expect(listener).not.toHaveBeenCalled();
    });

    it('구독 해제는 다른 리스너에 영향을 주지 않는다', () => {
      const store = createStore(0);
      const listener1 = jest.fn();
      const listener2 = jest.fn();
      const unsubscribe1 = store.subscribe(listener1);
      store.subscribe(listener2);
      unsubscribe1();
      store.setState(1);
      expect(listener1).not.toHaveBeenCalled();
      expect(listener2).toHaveBeenCalledTimes(1);
    });
  });
});

describe('useStore', () => {
  it('초기 상태를 반환한다', () => {
    const store = createStore(42);
    const { result } = renderHook(() => useStore(store));
    expect(result.current).toBe(42);
  });

  it('setState 후 최신 상태를 반환한다', () => {
    const store = createStore(0);
    const { result } = renderHook(() => useStore(store));

    act(() => {
      store.setState(99);
    });

    expect(result.current).toBe(99);
  });

  it('업데이터 함수로 setState해도 최신 상태를 반환한다', () => {
    const store = createStore({ count: 0 });
    const { result } = renderHook(() => useStore(store));

    act(() => {
      store.setState(prev => ({ count: prev.count + 1 }));
    });

    expect(result.current).toEqual({ count: 1 });
  });
});
