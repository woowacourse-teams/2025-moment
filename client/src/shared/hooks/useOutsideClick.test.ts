import { renderHook } from '@testing-library/react';
import { createRef } from 'react';
import { useOutsideClick } from './useOutsideClick';

const makeElement = () => {
  const el = document.createElement('div');
  document.body.appendChild(el);
  return el;
};

const clickOn = (target: Element) => {
  const event = new MouseEvent('mousedown', { bubbles: true });
  Object.defineProperty(event, 'target', { value: target, configurable: true });
  document.dispatchEvent(event);
};

describe('useOutsideClick', () => {
  let container: HTMLDivElement;
  let outside: HTMLDivElement;

  beforeEach(() => {
    container = makeElement();
    outside = makeElement();
  });

  afterEach(() => {
    container.remove();
    outside.remove();
  });

  it('isActive가 false이면 외부 클릭 시 callback이 호출되지 않는다', () => {
    const callback = jest.fn();
    const ref = createRef<HTMLElement>();
    (ref as React.MutableRefObject<HTMLElement>).current = container;

    renderHook(() => useOutsideClick({ ref, callback, isActive: false }));

    clickOn(outside);

    expect(callback).not.toHaveBeenCalled();
  });

  it('isActive가 true이고 ref 외부를 클릭하면 callback이 호출된다', () => {
    const callback = jest.fn();
    const ref = createRef<HTMLElement>();
    (ref as React.MutableRefObject<HTMLElement>).current = container;

    renderHook(() => useOutsideClick({ ref, callback, isActive: true }));

    clickOn(outside);

    expect(callback).toHaveBeenCalledTimes(1);
  });

  it('ref 내부를 클릭하면 callback이 호출되지 않는다', () => {
    const callback = jest.fn();
    const inner = document.createElement('div');
    container.appendChild(inner);
    const ref = createRef<HTMLElement>();
    (ref as React.MutableRefObject<HTMLElement>).current = container;

    renderHook(() => useOutsideClick({ ref, callback, isActive: true }));

    clickOn(inner);

    expect(callback).not.toHaveBeenCalled();
  });

  it('excludeRefs에 포함된 요소를 클릭해도 callback이 호출되지 않는다', () => {
    const callback = jest.fn();
    const excluded = makeElement();
    const ref = createRef<HTMLElement>();
    const excludeRef = createRef<HTMLElement>();
    (ref as React.MutableRefObject<HTMLElement>).current = container;
    (excludeRef as React.MutableRefObject<HTMLElement>).current = excluded;

    renderHook(() =>
      useOutsideClick({ ref, callback, isActive: true, excludeRefs: [excludeRef] }),
    );

    clickOn(excluded);

    expect(callback).not.toHaveBeenCalled();
    excluded.remove();
  });

  it('언마운트 후 외부 클릭 시 callback이 호출되지 않는다', () => {
    const callback = jest.fn();
    const ref = createRef<HTMLElement>();
    (ref as React.MutableRefObject<HTMLElement>).current = container;

    const { unmount } = renderHook(() => useOutsideClick({ ref, callback, isActive: true }));
    unmount();

    clickOn(outside);

    expect(callback).not.toHaveBeenCalled();
  });
});
