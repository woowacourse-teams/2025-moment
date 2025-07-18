import styled from '@emotion/styled';

export const StepContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
`;

export const InputGroup = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const Label = styled.label`
  font-size: 14px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
`;

export const Input = styled.input`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 8px;
  background-color: ${({ theme }) => theme.colors['gray-600_20']};
  color: ${({ theme }) => theme.colors.white};
  font-size: 16px;

  &:focus {
    outline: none;
    border-color: ${({ theme }) => theme.colors['yellow-500']};
    box-shadow: 0 0 0 2px ${({ theme }) => theme.colors['yellow-300']}40; /* 40은 hex opacity */
  }

  &::placeholder {
    color: ${({ theme }) => theme.colors['gray-400']};
    opacity: 0.6;
  }
`;

export const ErrorMessage = styled.span`
  font-size: 12px;
  color: ${({ theme }) => theme.colors['red-500']};
  margin-top: 4px;
`;
