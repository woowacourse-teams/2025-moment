import { colors } from '@/constants/colors';
import styled from '@emotion/styled';

export const Navbar = styled.nav`
  height: 80px;
  background-color: transparent;
  padding: 0 24px;
  color: ${colors.text.primary};
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
