import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';

const twinkle = keyframes`
  0%, 100% { 
    opacity: 0.3;
    transform: scale(1);
  }
  50% { 
    opacity: 1;
    transform: scale(1.1);
  }
`;

const twinkleGroup1 = keyframes`
  0%, 100% { opacity: 0.2; transform: scale(0.8); }
  50% { opacity: 0.8; transform: scale(1.2); }
`;

const twinkleGroup2 = keyframes`
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 0.9; transform: scale(1.1); }
`;

const twinkleGroup3 = keyframes`
  0%, 100% { opacity: 0.3; transform: scale(0.9); }
  50% { opacity: 1; transform: scale(1.3); }
`;

export const StarFieldWrapper = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
  will-change: transform;
`;

export const Star = styled.div<{
  size: number;
  left: number;
  top: number;
  animationDelay: number;
  group: number;
}>`
  position: absolute;
  width: ${props => props.size}px;
  height: ${props => props.size}px;
  background: ${({ theme }) => theme.colors.white};
  border-radius: 50%;
  left: ${props => props.left}%;
  top: ${props => props.top}%;

  animation: ${props => {
      switch (props.group) {
        case 1:
          return twinkleGroup1;
        case 2:
          return twinkleGroup2;
        case 3:
          return twinkleGroup3;
        default:
          return twinkle;
      }
    }}
    ${props => 4 + props.group}s ease-in-out infinite;

  animation-delay: ${props => props.animationDelay}s;

  will-change: transform, opacity;
  transform-origin: center;

  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
`;
