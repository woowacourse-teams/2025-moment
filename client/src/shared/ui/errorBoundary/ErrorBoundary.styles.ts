import styled from '@emotion/styled';

export const ErrorContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px 20px;
  text-align: center;
  background: ${({ theme }) => theme.colors['slate-900']};
  color: ${({ theme }) => theme.colors.white};
`;

export const ErrorTitle = styled.h1`
  font-size: 24px;
  margin-bottom: 16px;
  color: ${({ theme }) => theme.colors.white};
`;

export const ErrorMessage = styled.p`
  font-size: 16px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin-bottom: 32px;
  max-width: 400px;
`;

export const ErrorButton = styled.button`
  background-color: ${({ theme }) => theme.colors['yellow-500']};
  color: black;
  padding: 12px 24px;
  border-radius: 8px;
  border: none;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    filter: brightness(1.1);
    transform: translateY(-2px);
  }
`;
