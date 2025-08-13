import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';

const fillAnimation = keyframes`
from {
width: 0%}
to {
width: var(--target-width)}`;

export const EXPBarContainer = styled.div`
  width: 140px;
  height: 24px;
  background-color: transparent;
`;

export const EXPBar = styled.div`
  width: 100%;
  height: 100%;
  background-color: transparent;
  border-radius: 20px;
  border: 1px solid ${({ theme }) => theme.colors['white']};
`;

export const EXPBarProgress = styled.div<{ progress: number }>`
  width: ${({ progress }) => progress}%;
  height: 100%;
  border-radius: 100px;
  background: linear-gradient(to bottom, yellow, #a2d24c);
  animation: ${fillAnimation} 4s ease-in-out;
  --target-width: ${({ progress }) => progress}%;
`;
