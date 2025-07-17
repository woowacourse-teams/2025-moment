import styled from '@emotion/styled';

export const SignupFormWrapper = styled.form`
  width: 100%;
  max-width: 400px;
  min-height: 500px;
  padding: 32px;
  background-color: ${({ theme }) => theme.background.secondary};
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
`;
