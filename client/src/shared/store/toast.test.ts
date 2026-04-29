import { renderHook, act } from '@testing-library/react';
import { toast, useToasts } from './toast';

beforeEach(() => {
  jest.useFakeTimers();
  toast.clear();
});

afterEach(() => {
  jest.useRealTimers();
});

describe('toast', () => {
  describe('success / error / warning', () => {
    it('success 토스트를 추가한다', () => {
      const { result } = renderHook(() => useToasts());

      act(() => {
        toast.success('성공 메시지');
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].message).toBe('성공 메시지');
      expect(result.current.toasts[0].variant).toBe('success');
    });

    it('error 토스트를 추가한다', () => {
      const { result } = renderHook(() => useToasts());

      act(() => {
        toast.error('오류 발생');
      });

      expect(result.current.toasts[0].variant).toBe('error');
      expect(result.current.toasts[0].message).toBe('오류 발생');
    });

    it('warning 토스트를 추가한다', () => {
      const { result } = renderHook(() => useToasts());

      act(() => {
        toast.warning('경고 메시지');
      });

      expect(result.current.toasts[0].variant).toBe('warning');
    });
  });

  describe('message 토스트', () => {
    it('routeType과 link를 포함한 message 토스트를 추가한다', () => {
      const { result } = renderHook(() => useToasts());

      act(() => {
        toast.message('새 알림', 'moment', 3000, '/moments/1');
      });

      const t = result.current.toasts[0];
      expect(t.variant).toBe('message');
      expect(t.routeType).toBe('moment');
      expect(t.link).toBe('/moments/1');
    });
  });

  describe('중복 토스트 방지', () => {
    it('같은 메시지+variant의 토스트는 중복 추가되지 않는다', () => {
      const { result } = renderHook(() => useToasts());

      act(() => {
        toast.success('동일 메시지');
        toast.success('동일 메시지');
      });

      expect(result.current.toasts).toHaveLength(1);
    });

    it('다른 variant는 별도로 추가된다', () => {
      const { result } = renderHook(() => useToasts());

      act(() => {
        toast.success('메시지');
        toast.error('메시지');
      });

      expect(result.current.toasts).toHaveLength(2);
    });
  });

  describe('자동 제거', () => {
    it('기본 3초 후 토스트가 자동 제거된다', () => {
      const { result } = renderHook(() => useToasts());

      act(() => {
        toast.success('임시 메시지');
      });

      expect(result.current.toasts).toHaveLength(1);

      act(() => {
        jest.advanceTimersByTime(3000);
      });

      expect(result.current.toasts).toHaveLength(0);
    });

    it('커스텀 duration 후 자동 제거된다', () => {
      const { result } = renderHook(() => useToasts());

      act(() => {
        toast.success('짧은 토스트', 1000);
      });

      act(() => {
        jest.advanceTimersByTime(999);
      });
      expect(result.current.toasts).toHaveLength(1);

      act(() => {
        jest.advanceTimersByTime(1);
      });
      expect(result.current.toasts).toHaveLength(0);
    });
  });

  describe('dismiss / clear', () => {
    it('dismiss로 특정 토스트를 즉시 제거한다', () => {
      const { result } = renderHook(() => useToasts());

      let id: string;
      act(() => {
        id = toast.success('제거될 토스트');
      });

      act(() => {
        toast.dismiss(id);
      });

      expect(result.current.toasts).toHaveLength(0);
    });

    it('clear로 모든 토스트를 제거한다', () => {
      const { result } = renderHook(() => useToasts());

      act(() => {
        toast.success('1번');
        toast.error('2번');
        toast.warning('3번');
      });

      expect(result.current.toasts).toHaveLength(3);

      act(() => {
        toast.clear();
      });

      expect(result.current.toasts).toHaveLength(0);
    });
  });

  describe('ID 반환', () => {
    it('toastId를 반환한다', () => {
      let id = '';
      act(() => {
        id = toast.success('메시지');
      });
      expect(typeof id).toBe('string');
      expect(id).toMatch(/^toast-/);
    });
  });
});
