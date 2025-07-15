import { colors } from '@/constants/colors';
import styled from '@emotion/styled';

export const Navbar = styled.nav`
  height: 80px;
  background-color: ${colors.background.secondary};
  padding: 0 24px;
  color: ${colors.text.primary};
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 100;
`;

export const LogoContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
`;

export const Logo = styled.img`
  width: 180px;
  height: 180px;
  object-fit: contain;
  object-position: center;
  border-radius: 50%;

  &:hover {
    transform: scale(1.05);
    transition: transform 0.3s ease;
  }
`;
