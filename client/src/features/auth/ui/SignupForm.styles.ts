import styled from '@emotion/styled';

export const SignupFormWrapper = styled.div`
  width: 80%;
  max-width: 500px;
  min-height: 550px;
  max-height: 600px;
  padding: 32px;
  background-color: ${({ theme }) => theme.colors['slate-800_60']}; // #1E293B
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 28px;
`;

export const SignupFormContent = styled.div`
  width: 100%;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: start;
`;

export const ButtonContainer = styled.div`
  width: 100%;
  display: flex;
  justify-content: space-between;
`;
