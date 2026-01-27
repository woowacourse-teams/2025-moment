import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const BottomNavContainer = styled.nav`
  display: none;
  @media (max-width: 768px) {
    display: flex;
    justify-content: space-around;
    align-items: center;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 80px;
    background-color: #0b0b0b; /* Use theme color if available, fallback for now */
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    z-index: 1000;
    padding-bottom: env(safe-area-inset-bottom);
  }
`;

export const NavItem = styled.div<{ $isActive?: boolean; $shadow?: boolean }>`
  flex: 1;
  height: 100%;

  a {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    text-decoration: none;
    color: ${({ theme, $isActive }) => ($isActive ? theme.colors.white : theme.colors['gray-400'])};
    transition: color 0.2s;
  }

  ${({ theme, $shadow }) =>
    $shadow &&
    css`
      position: relative;
      &::after {
        content: '';
        position: absolute;
        top: 15px;
        right: 25%;
        width: 6px;
        height: 6px;
        background: ${theme.colors['yellow-300']};
        border-radius: 50%;
        box-shadow: 0 0 4px ${theme.colors['yellow-300']};
      }
    `}
`;

export const IconWrapper = styled.div`
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
`;

export const Label = styled.span`
  font-size: 12px;
  font-weight: 500;
`;
