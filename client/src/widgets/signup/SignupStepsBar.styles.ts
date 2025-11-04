import styled from '@emotion/styled';

export const StepsWrapper = styled.header`
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  width: 100%;
`;

export const StepsContainer = styled.nav`
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
    completed
      ? theme.colors['emerald-500']
      : active
        ? theme.colors['yellow-500']
        : theme.colors['slate-700']};
  transition: all 0.3s ease;
`;

export const StepConnector = styled.div<{ completed: boolean }>`
  width: 32px;
  height: 2px;
  background-color: ${({ completed, theme }) =>
    completed ? theme.colors['emerald-500'] : theme.colors['slate-700']};
  transition: all 0.3s ease;
`;

export const StepTitle = styled.div<{ active: boolean }>`
  font-size: 20px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
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
  color: ${({ theme }) => theme.colors['yellow-500']};
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
`;
