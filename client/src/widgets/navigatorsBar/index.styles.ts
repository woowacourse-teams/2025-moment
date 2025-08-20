import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const NavigatorsBarContainer = styled.div<{ $isNavBar?: boolean }>`
  display: flex;
  flex-direction: ${({ $isNavBar }) => ($isNavBar ? 'row' : 'column')};
  align-items: center;
  justify-content: center;
  gap: 60px;

  @media (max-width: 1024px) {
    gap: 10px;
  }
`;

export const LinkContainer = styled.div<{
  $isNavBar?: boolean;
  $shadow?: boolean;
  $isActive: boolean;
}>`
  ${({ theme, $shadow }) =>
    $shadow &&
    css`
      position: relative;
      padding: 4px;
      &::after {
        content: '';
        position: absolute;
        top: -0.6vh;
        right: -0.8vh;
        width: 10px;
        height: 10px;
        background: ${theme.colors['yellow-300']};
        border-radius: 50%;
        box-shadow:
          0 0 4px ${theme.colors['yellow-300']},
          0 0 8px ${theme.colors['yellow-300_80']};
        animation: dotPulse 2s ease-in-out infinite;
      }

      @keyframes dotPulse {
        0%,
        100% {
          transform: scale(0.6);
          box-shadow:
            0 0 2px ${theme.colors['yellow-300']},
            0 0 4px ${theme.colors['yellow-300_80']};
        }
        50% {
          transform: scale(1);
          box-shadow:
            0 0 4px ${theme.colors['yellow-300']},
            0 0 8px ${theme.colors['yellow-300_80']};
        }
      }
    `}

  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: ${({ $isNavBar }) => ($isNavBar ? 'row' : 'column')};
  gap: 12px;
  border-radius: 10px;

  &:hover {
    transform: scale(1.1);
    transition: transform 0.3s ease-in-out;
  }
`;

export const IconImage = styled.img`
  width: 40px;
  height: 40px;
`;

export const IconText = styled.p<{ $isActive?: boolean }>`
  font-size: 1.3rem;
  font-weight: 600;
  line-height: 14.52px;
  letter-spacing: -0.01em;
  color: ${({ theme, $isActive }) => ($isActive ? theme.colors['yellow-300'] : theme.colors.white)};

  @media (max-width: 1024px) {
    font-size: 1.1rem;
  }
`;
