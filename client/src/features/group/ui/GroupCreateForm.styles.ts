import styled from '@emotion/styled';

export const FormContainer = styled.form`
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  background: ${({ theme }) => theme.colors['slate-900']};
  border: 1px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 16px;
  padding: 24px 5%;

  @media (min-width: 768px) {
    gap: 28px;
    padding: 40px 6%;
    border-radius: 20px;
  }

  @media (min-width: 1024px) {
    padding: 48px 8%;
  }
`;

export const InputGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
`;

export const Label = styled.label`
  font-size: 14px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};

  @media (min-width: 768px) {
    font-size: 15px;
  }
`;

export const TextArea = styled.textarea`
  width: 100%;
  min-height: 120px;
  padding: 14px 16px;
  border: 1.5px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 12px;
  font-size: 15px;
  font-family: inherit;
  resize: vertical;
  background: ${({ theme }) => theme.colors['slate-800']};
  color: ${({ theme }) => theme.colors.white};
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;

  &:focus {
    outline: none;
    border-color: ${({ theme }) => theme.colors['yellow-500']};
    box-shadow: 0 0 0 3px ${({ theme }) => theme.colors['yellow-500']}20;
  }

  &::placeholder {
    color: ${({ theme }) => theme.colors['gray-600']};
  }

  @media (min-width: 768px) {
    min-height: 140px;
    padding: 16px 18px;
    font-size: 16px;
  }
`;

export const CharCount = styled.span`
  font-size: 13px;
  color: ${({ theme }) => theme.colors['gray-600']};
  text-align: right;
  margin-top: -2px;
`;

export const ButtonGroup = styled.div`
  display: flex;
  flex-direction: column-reverse;
  gap: 12px;
  margin-top: 8px;

  button {
    width: 100%;
    min-height: 52px;
    font-size: 16px;
    font-weight: 600;
  }

  @media (min-width: 640px) {
    flex-direction: row;
    justify-content: flex-end;
    gap: 16px;

    button {
      width: auto;
      min-width: 140px;
    }
  }
`;
