import { useImageFallback } from '@/shared/hooks';
import { ImgHTMLAttributes } from 'react';

interface OptimizedImageProps extends Omit<ImgHTMLAttributes<HTMLImageElement>, 'src'> {
  originalUrl: string;
  className?: string;
}

/**
 * webp 최적화 이미지를 로딩하고, 실패 시 원본 이미지로 fallback하는 컴포넌트
 * 1차 시도: upload-resize 경로의 webp 이미지
 * 2차 시도: upload 경로의 원본 이미지
 */
export const OptimizedImage = ({ originalUrl, className, ...props }: OptimizedImageProps) => {
  const { src, onError } = useImageFallback(originalUrl);

  return <img src={src} onError={onError} className={className} {...props} />;
};
