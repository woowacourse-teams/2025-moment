import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const NavigatorsBarContainer = styled.div<{ $isNavBar?: boolean }>`
  display: flex;
  flex-direction: ${({ $isNavBar }) => ($isNavBar ? 'row' : 'column')};
  align-items: center;
  justify-content: center;
  gap: 60px;

  @media (max-width: 1024px) {
    gap: 30px;
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
  align-items: center;
  justify-content: center;
  flex-direction: ${({ $isNavBar }) => ($isNavBar ? 'row' : 'column')};
  gap: 12px;
  border-radius: 10px;
  padding: 10px 20px;

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
