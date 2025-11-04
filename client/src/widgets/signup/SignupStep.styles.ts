import styled from '@emotion/styled';

export const StepContainer = styled.fieldset`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  border: none;
  padding: 0;
  margin: 0;
`;

export const EmailGroup = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
`;

export const InputGroup = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const EmailLabel = styled.label`
  font-size: 14px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
  margin-bottom: 6px;
`;

export const Label = styled.label`
  font-size: 14px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
`;

export const ErrorMessage = styled.span`
  font-size: 12px;
  color: ${({ theme }) => theme.colors['red-500']};
  margin-top: 4px;
  height: 18px;
  display: block;
  line-height: 1.5;
`;

export const SuccessMessage = styled.span`
  font-size: 12px;
  color: ${({ theme }) => theme.colors['emerald-500']};
  margin-top: 4px;
  height: 18px;
  display: block;
  line-height: 1.5;
`;

export const CheckExistContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;
