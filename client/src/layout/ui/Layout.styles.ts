import { theme } from '@/styles/theme';
import styled from '@emotion/styled';

export const Wrapper = styled.div`
  min-height: 100vh;
  background-color: ${theme.background.primary};
  color: ${theme.text.primary};
  font-family:
    system-ui,
    -apple-system,
    sans-serif;
`;

export const Navbar = styled.nav`
  height: 80px;
  background-color: ${theme.background.secondary};
  padding: 0 24px;
  color: ${theme.text.primary};
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

export const Main = styled.main`
  padding: 24px;
  min-height: calc(100vh - 80px);
`;
