import { renderHook, act } from '@testing-library/react';
import { useImageFallback } from './useImageFallback';

const OPTIMIZED = 'https://cdn.example.com/upload-resize/photo.webp';
const ORIGINAL = 'https://cdn.example.com/upload/photo.jpg';
const DEFAULT = '/images/no-image.webp';

describe('useImageFallback', () => {
  describe('초기 src 설정', () => {
    it('optimizedImageUrl이 있으면 최적화 이미지를 먼저 사용한다', () => {
      const { result } = renderHook(() =>
        useImageFallback({ originalImageUrl: ORIGINAL, optimizedImageUrl: OPTIMIZED }),
      );
      expect(result.current.src).toBe(OPTIMIZED);
    });

    it('optimizedImageUrl이 없으면 originalImageUrl을 사용한다', () => {
      const { result } = renderHook(() =>
        useImageFallback({ originalImageUrl: ORIGINAL, optimizedImageUrl: null }),
      );
      expect(result.current.src).toBe(ORIGINAL);
    });

    it('둘 다 없으면 기본 이미지를 사용한다', () => {
      const { result } = renderHook(() =>
        useImageFallback({ originalImageUrl: null, optimizedImageUrl: null }),
      );
      expect(result.current.src).toBe(DEFAULT);
    });
  });

  describe('onError 폴백 체인', () => {
    it('1차 실패: optimized → original로 폴백한다', () => {
      const { result } = renderHook(() =>
        useImageFallback({ originalImageUrl: ORIGINAL, optimizedImageUrl: OPTIMIZED }),
      );

      act(() => {
        result.current.onError();
      });

      expect(result.current.src).toBe(ORIGINAL);
    });

    it('2차 실패: original → 기본 이미지로 폴백한다', () => {
      const { result } = renderHook(() =>
        useImageFallback({ originalImageUrl: ORIGINAL, optimizedImageUrl: OPTIMIZED }),
      );

      act(() => {
        result.current.onError(); // optimized → original
      });
      act(() => {
        result.current.onError(); // original → default
      });

      expect(result.current.src).toBe(DEFAULT);
    });

    it('originalImageUrl만 있을 때 실패하면 기본 이미지로 폴백한다', () => {
      const { result } = renderHook(() =>
        useImageFallback({ originalImageUrl: ORIGINAL, optimizedImageUrl: null }),
      );

      act(() => {
        result.current.onError();
      });

      expect(result.current.src).toBe(DEFAULT);
    });

    it('둘 다 없을 때 실패해도 기본 이미지를 유지한다', () => {
      const { result } = renderHook(() =>
        useImageFallback({ originalImageUrl: null, optimizedImageUrl: null }),
      );

      act(() => {
        result.current.onError();
      });

      expect(result.current.src).toBe(DEFAULT);
    });
  });
});
