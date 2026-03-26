import { useEffect, useState } from 'react';
import { convertToWebp, getFallbackImageUrl } from '@/shared/utils/convertToWebp';

interface UseImageFallbackReturn {
  src: string;
  onError: () => void;
}

const DEFAULT_IMAGE_FALLBACK_SRC = '/images/no-image.webp';

/**
 * 이미지 로딩 실패 시 fallback 처리하는 커스텀 훅
 * 1차 시도: webp 확장자 이미지
 * 2차 시도: upload-resize → upload 경로로 원본 이미지
 * 3차 시도: 프로젝트 내부 기본 이미지
 * @param originalUrl - 백엔드에서 받은 원본 이미지 URL
 * @returns src와 onError 핸들러
 */
export const useImageFallback = (originalUrl: string): UseImageFallbackReturn => {
  const [src, setSrc] = useState<string>(() => convertToWebp(originalUrl));
  const [failureCount, setFailureCount] = useState<number>(0);

  useEffect(() => {
    setSrc(convertToWebp(originalUrl));
    setFailureCount(0);
  }, [originalUrl]);

  const handleError = () => {
    if (failureCount === 0) {
      // 첫 번째 실패: webp → 원본 확장자 + upload 경로
      setSrc(getFallbackImageUrl(originalUrl));
      setFailureCount(1);
      return;
    }

    if (failureCount === 1) {
      // 두 번째 실패: 프로젝트 내부 기본 이미지
      setSrc(DEFAULT_IMAGE_FALLBACK_SRC);
      setFailureCount(2);
      return;
    }
  };

  return {
    src,
    onError: handleError,
  };
};
