import { useState } from 'react';

interface UseImageFallbackProps {
  originalImageUrl: string | null;
  optimizedImageUrl: string | null;
}

interface UseImageFallbackReturn {
  src: string;
  onError: () => void;
}

const DEFAULT_IMAGE_FALLBACK_SRC = '/images/no-image.webp';

/**
 * 이미지 로딩 실패 시 fallback 처리하는 커스텀 훅
 * 1차 시도: optimizedImageUrl (최적화된 webp 이미지)
 * 2차 시도: originalImageUrl (원본 이미지)
 * 3차 시도: 프로젝트 내부 기본 이미지
 * @param props - originalImageUrl과 optimizedImageUrl을 포함하는 객체
 * @returns src와 onError 핸들러
 */
export const useImageFallback = ({
  originalImageUrl,
  optimizedImageUrl,
}: UseImageFallbackProps): UseImageFallbackReturn => {
  const getInitialSrc = (): string => {
    if (optimizedImageUrl) return optimizedImageUrl;
    if (originalImageUrl) return originalImageUrl;
    return DEFAULT_IMAGE_FALLBACK_SRC;
  };

  const [src, setSrc] = useState<string>(getInitialSrc);
  const [failureCount, setFailureCount] = useState<number>(0);

  const handleError = () => {
    if (failureCount === 0 && optimizedImageUrl && originalImageUrl) {
      // 첫 번째 실패: optimized → original로 폴백
      setSrc(originalImageUrl);
      setFailureCount(1);
      return;
    }

    // 두 번째 실패 또는 fallback이 없는 경우: 기본 이미지
    setSrc(DEFAULT_IMAGE_FALLBACK_SRC);
    setFailureCount(2);
  };

  return {
    src,
    onError: handleError,
  };
};
