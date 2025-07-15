import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';

const twinkle = keyframes`
  0%, 100% { 
    opacity: 0.5;
  }
  50% { 
    opacity: 1;
  }
`;

export const StarFieldWrapper = styled.div`
  position: fixed;
  top: 80px;
  left: 0;
  width: 100%;
  height: calc(100% - 80px);
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
`;

export const Star = styled.div<{ size: number; left: number; top: number; animationDelay: number }>`
  position: absolute;
  width: ${props => props.size}px;
  height: ${props => props.size}px;
  background: #ffffff;
  border-radius: 50%;
  left: ${props => props.left}%;
  top: ${props => props.top}%;
  animation: ${twinkle} 3s ease-in-out infinite;
  animation-delay: ${props => props.animationDelay}s;
  box-shadow: 0 0 4px #ffffff;
`;
