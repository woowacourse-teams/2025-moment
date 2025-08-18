import styled from '@emotion/styled';

export const PasswordPageWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: 24px;
`;

export const PasswordPageTitle = styled.div`
  font-size: 24px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['white']};
`;

export const PasswordPageContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
`;

export const PasswordPageInput = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const ErrorMessage = styled.p`
  color: ${({ theme }) => theme.colors['red-500']};
`;
