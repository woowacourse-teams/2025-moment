import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const LazyImageContainer = styled.div`
  position: relative;
  display: inline-block;
`;

const blackHoleStyles = css`
  width: 150px;
  height: 150px;

  @media (max-width: 1200px) {
    width: 100px;
    height: 100px;
  }
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
      case 'blackHole':
        return blackHoleStyles;
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

export const Image = styled.img<{
  $isLoading: boolean;
  $hasError: boolean;
  $variant: string;
}>`
  display: ${({ $isLoading, $hasError }) => ($isLoading || $hasError ? 'none' : 'block')};
  transition: opacity 0.3s ease-in-out;
  opacity: ${({ $isLoading }) => ($isLoading ? 0 : 1)};

  ${({ $variant }) => {
    switch ($variant) {
      case 'blackHole':
        return blackHoleStyles;
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
