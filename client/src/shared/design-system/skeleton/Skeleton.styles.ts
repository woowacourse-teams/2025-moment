import { theme } from '@/app/styles/theme';
import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';

const shimmer = keyframes`
  0% {
    background-position: -200px 0;
  }
  100% {
    background-position: calc(200px + 100%) 0;
  }
`;

export const SkeletonContainer = styled.div<{
  width: string | number;
  height: string | number;
  borderRadius: string | number;
}>`
  width: ${({ width }) => (typeof width === 'number' ? `${width}px` : width)};
  height: ${({ height }) => (typeof height === 'number' ? `${height}px` : height)};
  border-radius: ${({ borderRadius }) =>
    typeof borderRadius === 'number' ? `${borderRadius}px` : borderRadius};
  background: linear-gradient(
    90deg,
    ${theme.colors['slate-800_60']} 25%,
    ${theme.colors['gray-700']} 50%,
    ${theme.colors['slate-800_60']} 75%
  );
  background-size: 200px 100%;
  animation: ${shimmer} 4s ease-in-out infinite;
`;

export const SkeletonTextContainer = styled.div<{ gap: string }>`
  display: flex;
  flex-direction: column;
  gap: ${({ gap }) => gap};
`;

export const SkeletonCardContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;
