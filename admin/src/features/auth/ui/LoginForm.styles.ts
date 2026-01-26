import styled from '@emotion/styled';

export const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 2rem;
  background-color: #1f1f1f;
  border-radius: 8px;
  min-width: 320px;
`;

export const Title = styled.h1`
  font-size: 1.5rem;
  font-weight: 600;
  text-align: center;
  margin-bottom: 1rem;
`;

export const InputGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

export const Label = styled.label`
  font-size: 0.875rem;
  font-weight: 500;
`;

export const Input = styled.input`
  padding: 0.75rem;
  border: 1px solid #333;
  border-radius: 4px;
  background-color: #2a2a2a;
  color: white;
  font-size: 1rem;

  &:focus {
    outline: none;
    border-color: #3b82f6;
  }
`;

export const SubmitButton = styled.button`
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  background-color: #3b82f6;
  color: white;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  margin-top: 0.5rem;

  &:hover {
    background-color: #2563eb;
  }

  &:disabled {
    background-color: #4b5563;
    cursor: not-allowed;
  }
`;

export const ErrorMessage = styled.p`
  color: #ef4444;
  font-size: 0.875rem;
  text-align: center;
`;
