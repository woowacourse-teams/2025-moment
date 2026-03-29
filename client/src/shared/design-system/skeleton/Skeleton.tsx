import React from 'react';
import * as S from './Skeleton.styles';

export interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  width?: string | number;
  height?: string | number;
  borderRadius?: string | number;
}

export const Skeleton = React.forwardRef<HTMLDivElement, SkeletonProps>(
  ({ width = '100%', height = '1rem', borderRadius = '4px', ...props }, ref) => {
    return (
      <S.SkeletonContainer
        ref={ref}
        $width={width}
        $height={height}
        $borderRadius={borderRadius}
        {...props}
      />
    );
  },
);

Skeleton.displayName = 'Skeleton';

interface SkeletonTextProps {
  lines?: number;
  lineHeight?: string;
  gap?: string;
}

export const SkeletonText: React.FC<SkeletonTextProps> = ({
  lines = 1,
  lineHeight = '1rem',
  gap = '0.5rem',
}) => {
  return (
    <S.SkeletonTextContainer gap={gap}>
      {Array.from({ length: lines }).map((_, index) => (
        <Skeleton key={index} height={lineHeight} width={index === lines - 1 ? '75%' : '100%'} />
      ))}
    </S.SkeletonTextContainer>
  );
};
