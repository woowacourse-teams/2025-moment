import { Step } from '@/features/auth/types';
import styled from '@emotion/styled';
import { BadgeCheck, Lock, User } from 'lucide-react';

interface SignupStepBarProps {
  step: Step;
}

const StepsWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  width: 100%;
`;

const StepsContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
  width: 100%;
`;

const StepDot = styled.div<{ active: boolean; completed: boolean }>`
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: ${({ active, completed, theme }) =>
    completed ? theme.success.default : active ? theme.colors.secondary : theme.border.primary};
  transition: all 0.3s ease;
`;

const StepConnector = styled.div<{ completed: boolean }>`
  width: 32px;
  height: 2px;
  background-color: ${({ completed, theme }) =>
    completed ? theme.success.default : theme.border.primary};
  transition: all 0.3s ease;
`;

const StepTitle = styled.div<{ active: boolean }>`
  font-size: 20px;
  font-weight: 600;
  color: ${({ theme }) => theme.text.primary};
  text-align: center;
  transition: all 0.3s ease;
  text-align: center;
`;

const SignupFormTitle = styled.div`
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
`;

const SignupFormIcon = styled.div`
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

export const SignupStepBar = ({ step }: SignupStepBarProps) => {
  const getCurrentStepNumber = (currentStep: Step): number => {
    switch (currentStep) {
      case 'step1':
        return 1;
      case 'step2':
        return 2;
      case 'step3':
        return 3;
      default:
        return 1;
    }
  };

  const getStepIcon = (currentStep: Step): React.ReactNode => {
    switch (currentStep) {
      case 'step1':
        return <Lock />;
      case 'step2':
        return <User />;
      case 'step3':
        return <BadgeCheck />;
    }
  };

  const getStepTitle = (currentStep: Step): string => {
    switch (currentStep) {
      case 'step1':
        return '아이디/비밀번호';
      case 'step2':
        return '프로필 설정';
      case 'step3':
        return '완료';
      default:
        return '아이디/비밀번호';
    }
  };

  const currentStepNumber = getCurrentStepNumber(step);
  const currentTitle = getStepTitle(step);

  return (
    <StepsWrapper>
      <StepsContainer>
        <StepDot active={currentStepNumber === 1} completed={currentStepNumber > 1} />
        <StepConnector completed={currentStepNumber > 1} />
        <StepDot active={currentStepNumber === 2} completed={currentStepNumber > 2} />
        <StepConnector completed={currentStepNumber > 2} />
        <StepDot active={currentStepNumber === 3} completed={false} />
      </StepsContainer>
      <SignupFormTitle>
        <SignupFormIcon>{getStepIcon(step)}</SignupFormIcon>
        <StepTitle active={true}>{currentTitle}</StepTitle>
      </SignupFormTitle>
    </StepsWrapper>
  );
};
