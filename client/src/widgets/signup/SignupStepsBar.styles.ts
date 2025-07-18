import styled from '@emotion/styled';

export const StepsWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  width: 100%;
`;

export const StepsContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
  width: 100%;
`;

export const StepDot = styled.div<{ active: boolean; completed: boolean }>`
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: ${({ active, completed, theme }) =>
    completed ? theme.success.default : active ? theme.colors.secondary : theme.border.primary};
  transition: all 0.3s ease;
`;

export const StepConnector = styled.div<{ completed: boolean }>`
  width: 32px;
  height: 2px;
  background-color: ${({ completed, theme }) =>
    completed ? theme.success.default : theme.border.primary};
  transition: all 0.3s ease;
`;

export const StepTitle = styled.div<{ active: boolean }>`
  font-size: 20px;
  font-weight: 600;
  color: ${({ theme }) => theme.text.primary};
  text-align: center;
  transition: all 0.3s ease;
  text-align: center;
`;

export const SignupFormTitle = styled.div`
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
`;

export const SignupFormIcon = styled.div`
  width: 30px;
  height: 30px;
  color: ${({ theme }) => theme.colors.secondary};
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-center
  margin-bottom: 16px;
`;
