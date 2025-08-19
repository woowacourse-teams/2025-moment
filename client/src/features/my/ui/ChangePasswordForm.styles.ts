import styled from '@emotion/styled';

export const ChangePassWordFormWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  align-items: center;
  justify-content: center;
  width: 100%;
`;

export const ChangePasswordFormContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
`;

export const ChangePasswordFormInput = styled.input`
  width: 100%;
  height: 40px;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 16px;
  font-weight: 600;
  background-color: ${({ theme }) => theme.colors['gray-600_20']};
  color: ${({ theme }) => theme.colors['gray-400']};
  &::placeholder {
    color: ${({ theme }) => theme.colors['gray-400']};
  }
`;

export const ErrorMessage = styled.p`
  color: ${({ theme }) => theme.colors['red-500']};
`;
