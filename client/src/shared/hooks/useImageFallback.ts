import { useState } from 'react';
import { convertToWebp, getFallbackImageUrl } from '@/shared/utils/convertToWebp';

interface UseImageFallbackReturn {
  src: string;
  onError: () => void;
}

/**
 * 이미지 로딩 실패 시 fallback 처리하는 커스텀 훅
 * 1차 시도: webp 확장자 이미지
 * 2차 시도: upload-resize → upload 경로로 원본 이미지
 * @param originalUrl - 백엔드에서 받은 원본 이미지 URL
 * @returns src와 onError 핸들러
 */
export const useImageFallback = (originalUrl: string): UseImageFallbackReturn => {
  const [src, setSrc] = useState<string>(convertToWebp(originalUrl));
  const [failedOnce, setFailedOnce] = useState<boolean>(false);

  const handleError = () => {
    if (!failedOnce) {
      // 첫 번째 실패: webp → 원본 확장자 + upload 경로
      setFailedOnce(true);
      setSrc(getFallbackImageUrl(originalUrl));
    }
    // 두 번째 실패: 더 이상 fallback 없음 (broken image 표시)
  };

  return {
    src,
    onError: handleError,
  };
};
