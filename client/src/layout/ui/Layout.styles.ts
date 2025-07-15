import { colors } from '@/constants/colors';
import styled from '@emotion/styled';

export const Wrapper = styled.div`
  min-height: 100vh;
  background-color: #0a0a0f;
  color: ${colors.text.primary};
  font-family:
    system-ui,
    -apple-system,
    sans-serif;
  position: relative;
  overflow: hidden;
`;

export const Main = styled.main`
  min-height: calc(100vh - 80px);
  position: relative;
  z-index: 10;
`;
