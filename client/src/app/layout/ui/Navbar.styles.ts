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
  width: 50px;
  height: 40px;
`;

export const LevelIconWrapper = styled.div`
  position: relative;
  display: inline-block;
`;

export const LoadingSkeleton = styled.div`
  width: 50px;
  height: 40px;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.1) 25%,
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.1) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 8px;
  @keyframes shimmer {
    0% {
      background-position: 200% 0;
    }
    100% {
      background-position: -200% 0;
    }
  }
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
