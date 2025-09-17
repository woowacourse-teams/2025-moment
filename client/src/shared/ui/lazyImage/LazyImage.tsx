import React, { Suspense, useState } from 'react';
import { Skeleton } from '../skeleton/Skeleton';
import * as S from './LazyImage.styles';

interface LazyImageProps extends Omit<React.ImgHTMLAttributes<HTMLImageElement>, 'src' | 'alt'> {
  src: string;
  alt: string;
  width?: string | number;
  height?: string | number;
  borderRadius?: string | number;
  skeletonClassName?: string;
  variant?: 'blackHole' | 'icon' | 'character' | 'levelIcon' | 'default';
  eager?: boolean; // navbar 등에서 즉시 로딩용
}

const ImageComponent: React.FC<LazyImageProps> = ({
  src,
  alt,
  width,
  height,
  borderRadius = '4px',
  variant = 'default',
  eager = false,
  ...imgProps
}) => {
  const [hasError, setHasError] = useState(false);

  const handleError = () => {
    setHasError(true);
  };

  if (hasError) {
    return (
      <S.ErrorFallback
        width={width}
        height={height}
        $borderRadius={borderRadius}
        $variant={variant}
      >
        <S.ErrorIcon>📷</S.ErrorIcon>
      </S.ErrorFallback>
    );
  }

  return (
    <S.Image
      {...imgProps}
      src={src}
      alt={alt}
      loading={eager ? 'eager' : 'lazy'}
      onError={handleError}
      $isLoading={false}
      $hasError={false}
      $variant={variant}
    />
  );
};

export const LazyImage: React.FC<LazyImageProps> = ({
  width,
  height,
  borderRadius = '4px',
  skeletonClassName,
  eager = false,
  ...props
}) => {
  if (eager) {
    return <ImageComponent {...props} width={width} height={height} borderRadius={borderRadius} eager />;
  }

  return (
    <S.LazyImageContainer>
      <Suspense
        fallback={
          <Skeleton
            width={width}
            height={height}
            borderRadius={borderRadius}
            className={skeletonClassName}
          />
        }
      >
        <ImageComponent {...props} width={width} height={height} borderRadius={borderRadius} />
      </Suspense>
    </S.LazyImageContainer>
  );
};