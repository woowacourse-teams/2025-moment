import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const NavigatorsBarContainer = styled.div<{ $isNavBar?: boolean; $shadow?: boolean }>`
  ${({ theme, $shadow }) =>
    $shadow &&
    css`
      box-shadow: 0px 0px 15px ${theme.colors['yellow-300_80']};
      animation: shadowPulse 2s ease-in-out infinite;

      @keyframes shadowPulse {
        0%,
        100% {
          box-shadow: 0px 0px 10px ${theme.colors['yellow-300_80']};
        }
        50% {
          box-shadow: 0px 0px 25px ${theme.colors['yellow-300_80']};
        }
      }
    `}

  display: flex;
  flex-direction: ${({ $isNavBar }) => ($isNavBar ? 'row' : 'column')};
  align-items: center;
  justify-content: center;
  gap: 60px;

  @media (max-width: 1024px) {
    gap: 30px;
  }
`;

export const LinkContainer = styled.div<{ $isNavBar?: boolean }>`
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: ${({ $isNavBar }) => ($isNavBar ? 'row' : 'column')};
  gap: 12px;

  &:hover {
    transform: scale(1.1);
    transition: transform 0.3s ease-in-out;
  }
`;

export const IconImage = styled.img`
  width: 40px;
  height: 40px;
`;

export const IconText = styled.p`
  font-size: 1.3rem;
  font-weight: 600;
  line-height: 14.52px;
  letter-spacing: -0.01em;

  @media (max-width: 1024px) {
    font-size: 1.1rem;
  }
`;
