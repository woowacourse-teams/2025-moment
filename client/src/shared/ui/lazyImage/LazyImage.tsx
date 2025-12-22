import React, { useState } from 'react';
import { Skeleton } from '../../design-system/skeleton/Skeleton';
import * as S from './LazyImage.styles';

interface LazyImageProps extends Omit<React.ImgHTMLAttributes<HTMLImageElement>, 'src' | 'alt'> {
  src: string;
  alt: string;
  width?: string | number;
  height?: string | number;
  borderRadius?: string | number;
  skeletonClassName?: string;
  variant?: 'icon' | 'character' | 'levelIcon' | 'default';
  eager?: boolean;
}

export const LazyImage: React.FC<LazyImageProps> = ({
  src,
  alt,
  width,
  height,
  borderRadius = '4px',
  skeletonClassName,
  variant = 'default',
  eager = false,
  ...imgProps
}) => {
  const [isLoaded, setIsLoaded] = useState(false);
  const [hasError, setHasError] = useState(false);

  const handleLoad = () => {
    setIsLoaded(true);
  };

  const handleError = () => {
    setHasError(true);
  };

  return (
    <S.LazyImageContainer>
      <S.PlaceholderWrapper $isVisible={!isLoaded && !hasError}>
        <Skeleton
          width={width}
          height={height}
          borderRadius={borderRadius}
          className={skeletonClassName}
        />
      </S.PlaceholderWrapper>

      <S.ErrorWrapper $isVisible={hasError}>
        <S.ErrorFallback
          width={width}
          height={height}
          $borderRadius={borderRadius}
          $variant={variant}
        >
          <S.ErrorIcon>📷</S.ErrorIcon>
        </S.ErrorFallback>
      </S.ErrorWrapper>

      <S.Image
        {...imgProps}
        src={src}
        alt={alt}
        loading={eager ? 'eager' : 'lazy'}
        onLoad={handleLoad}
        onError={handleError}
        $variant={variant}
        $isVisible={isLoaded && !hasError}
        style={imgProps.style}
      />
    </S.LazyImageContainer>
  );
};
