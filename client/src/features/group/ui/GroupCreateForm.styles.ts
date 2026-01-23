import styled from '@emotion/styled';

export const FormContainer = styled.form`
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
`;

export const InputGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const Label = styled.label`
  font-size: 14px;
  font-weight: 500;
  color: ${({ theme }) => theme.colors.white};
`;

export const TextArea = styled.textarea`
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border: 1px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  background: ${({ theme }) => theme.colors['slate-800']};
  color: ${({ theme }) => theme.colors.white};

  &:focus {
    outline: none;
    border-color: ${({ theme }) => theme.colors['yellow-500']};
  }

  &::placeholder {
    color: ${({ theme }) => theme.colors['gray-600']};
  }
`;

export const CharCount = styled.span`
  font-size: 12px;
  color: ${({ theme }) => theme.colors['gray-600']};
  text-align: right;
`;

export const ButtonGroup = styled.div`
  display: flex;
  gap: 12px;
  justify-content: flex-end;
`;
