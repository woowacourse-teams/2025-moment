import { colors } from '@/constants/colors';
import styled from '@emotion/styled';

export const Wrapper = styled.div`
  height: 100vh;
  background-color: #0a0a0f;
  color: ${colors.text.primary};
  font-family:
    system-ui,
    -apple-system,
    sans-serif;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
`;

export const Main = styled.main`
  flex: 1;
  position: relative;
  z-index: 10;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
`;
