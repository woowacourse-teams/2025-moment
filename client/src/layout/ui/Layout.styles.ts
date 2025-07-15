import { colors } from '@/constants/colors';
import styled from '@emotion/styled';

export const Wrapper = styled.div`
  min-height: 100vh;
  background-color: ${colors.background.primary};
  color: ${colors.text.primary};
  font-family:
    system-ui,
    -apple-system,
    sans-serif;
`;

export const Navbar = styled.nav`
  background-color: ${colors.background.secondary};
  padding: 16px 24px;
  color: ${colors.text.primary};
`;

export const Main = styled.main`
  padding: 24px;
  min-height: calc(100vh - 80px);
`;
