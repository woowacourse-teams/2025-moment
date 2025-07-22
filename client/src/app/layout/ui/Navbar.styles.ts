import styled from '@emotion/styled';

interface NavItemProps {
  $isActive?: boolean;
}

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
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
`;

export const NavItems = styled.div`
  display: flex;
  align-items: center;
  gap: 24px;
`;

export const NavItem = styled.div<NavItemProps>`
  display: flex;
  align-items: center;
  gap: 24px;
  color: ${({ theme, $isActive }) => ($isActive ? theme.colors.white : theme.colors['gray-400'])};
  font-size: 1.2rem;
  font-weight: 600;

  &:hover {
    color: ${({ theme }) => theme.colors.white};
  }

  transition: color 0.3s ease;
`;
