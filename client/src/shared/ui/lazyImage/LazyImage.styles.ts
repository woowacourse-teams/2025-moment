import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const LazyImageContainer = styled.div`
  position: relative;
  display: inline-block;
`;

const iconStyles = css`
  width: 40px;
  height: 40px;
`;

const characterStyles = css`
  width: 250px;
  object-fit: contain;

  @media (max-width: 768px) {
    width: 180px;
  }
`;

const levelIconStyles = css`
  width: 50px;
  height: 50px;
  object-fit: contain;

  @media (max-width: 768px) {
    width: 25px;
    height: 25px;
  }
`;

export const ErrorFallback = styled.div<{
  width?: string | number;
  height?: string | number;
  $borderRadius?: string | number;
  $variant: string;
}>`
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: ${({ $borderRadius }) => $borderRadius || '4px'};
  width: ${({ width }) => (typeof width === 'number' ? `${width}px` : width || '100%')};
  height: ${({ height }) => (typeof height === 'number' ? `${height}px` : height || '100%')};

  ${({ $variant }) => {
    switch ($variant) {
      case 'icon':
        return iconStyles;
      case 'character':
        return characterStyles;
      case 'levelIcon':
        return levelIconStyles;
      default:
        return '';
    }
  }}
`;

export const ErrorIcon = styled.span`
  font-size: 24px;
  opacity: 0.5;
`;

export const PlaceholderWrapper = styled.div<{
  $isVisible: boolean;
}>`
  position: absolute;
  top: 0;
  left: 0;
  opacity: ${({ $isVisible }) => ($isVisible ? 1 : 0)};
  transition: opacity 0.3s ease;
  pointer-events: none;
`;

export const ErrorWrapper = styled.div<{
  $isVisible: boolean;
}>`
  position: absolute;
  top: 0;
  left: 0;
  opacity: ${({ $isVisible }) => ($isVisible ? 1 : 0)};
  transition: opacity 0.3s ease;
  pointer-events: ${({ $isVisible }) => ($isVisible ? 'auto' : 'none')};
`;

export const Image = styled.img<{
  $variant: string;
  $isVisible: boolean;
}>`
  opacity: ${({ $isVisible }) => ($isVisible ? 1 : 0)};
  transition: opacity 0.3s ease;

  ${({ $variant }) => {
    switch ($variant) {
      case 'icon':
        return iconStyles;
      case 'character':
        return characterStyles;
      case 'levelIcon':
        return levelIconStyles;
      default:
        return '';
    }
  }}
`;
