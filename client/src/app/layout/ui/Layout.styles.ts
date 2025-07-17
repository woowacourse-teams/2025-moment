import styled from '@emotion/styled';

export const Wrapper = styled.div`
  min-height: 100vh;
  background-color: ${({ theme }) => theme.colors.navy900};
  color: ${({ theme }) => theme.colors.white};
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
