import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const Navbar = styled.nav`
  height: 80px;
  background-color: transparent;
  padding: 0 24px;
  color: ${({ theme }) => theme.colors.white};
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  backdrop-filter: blur(10px);
`;

export const LevelIcon = styled.img`
  width: 40px;
  height: 40px;
`;

export const NavItem = styled.div<{ $isActive?: boolean }>`
  display: flex;
  align-items: center;
  gap: 24px;
  color: ${({ theme, $isActive }) => ($isActive ? theme.colors.white : theme.colors['gray-400'])};
  font-size: 1.1rem;
  font-weight: 600;

  cursor: pointer;

  &:active {
    color: ${({ theme }) => theme.colors.white};
  }

  &:hover {
    scale: 1.05;
  }

  transition: all 0.3s ease;
`;

export const DesktopNavItems = styled.div`
  display: flex;
  align-items: center;
  gap: 24px;

  @media (max-width: 768px) {
    display: none;
  }
`;

export const DesktopAuthButton = styled.div`
  display: flex;
  align-items: center;
  margin-right: 24px;

  @media (max-width: 768px) {
    display: none;
    margin-right: 0;
  }
`;

export const DropdownButton = styled.button<{ $isOpen: boolean }>`
  display: none;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: transparent;
  border: none;
  cursor: pointer;
  z-index: 101;
  color: ${({ theme }) => theme.colors.white};
  font-size: 24px;
  transition: all 0.3s ease;

  &:hover {
    color: ${({ theme }) => theme.colors['yellow-300']};
    transform: scale(1.1);
  }

  @media (max-width: 768px) {
    display: flex;
  }
`;

export const MobileMenu = styled.div<{ $isOpen: boolean }>`
  display: none;
  position: fixed;
  top: 80px;
  left: 0;
  right: 0;
  background: ${({ theme }) => theme.colors['gray-800']};
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 99;
  transform: translateY(-200%);
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  ${({ $isOpen }) =>
    $isOpen &&
    `
    transform: translateY(0);
    opacity: 1;
  `}

  @media (max-width: 768px) {
    display: block;
  }
`;

export const MobileMenuContent = styled.div`
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
`;

export const MobileNavItems = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

export const MobileNavItem = styled.div<{ $isActive?: boolean; $shadow?: boolean }>`
  padding: 16px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
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

  a {
    color: ${({ theme, $isActive }) =>
      $isActive ? theme.colors['yellow-300'] : theme.colors.white};
    font-size: 1.2rem;
    font-weight: 600;
    text-decoration: none;
    display: block;
    transition: all 0.3s ease;
  }

  &:hover a {
    color: ${({ theme }) => theme.colors['yellow-300']};
    transform: translateX(8px);
  }

  &:last-child {
    border-bottom: none;
  }
`;
