import { colors } from '@/app/styles/colors';
import styled from '@emotion/styled';

export const Wrapper = styled.div`
  min-height: 100vh;
  background-color: ${colors.background.primary};
  color: ${colors.text.primary};
  font-family: inherit;
  position: relative;
  display: flex;
  flex-direction: column;
`;

export const Main = styled.main`
  position: relative;
  z-index: 10;
  padding-top: 80px;
  flex: 1;
`;
