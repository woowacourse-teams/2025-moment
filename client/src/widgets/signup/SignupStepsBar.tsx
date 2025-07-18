import { Step } from '@/shared/types/step';
import { BadgeCheck, Lock, User } from 'lucide-react';
import * as S from './SignupStepsBar.styles';

interface SignupStepBarProps {
  step: Step;
}


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
        return '정보 확인';
      default:
        return '아이디/비밀번호';
    }
  };

  const currentStepNumber = getCurrentStepNumber(step);
  const currentTitle = getStepTitle(step);

  return (
    <S.StepsWrapper>
      <S.StepsContainer>
        <S.StepDot active={currentStepNumber === 1} completed={currentStepNumber > 1} />
        <S.StepConnector completed={currentStepNumber > 1} />
        <S.StepDot active={currentStepNumber === 2} completed={currentStepNumber > 2} />
        <S.StepConnector completed={currentStepNumber > 2} />
        <S.StepDot active={currentStepNumber === 3} completed={false} />
      </S.StepsContainer>
      <S.SignupFormTitle>
        <S.SignupFormIcon>{getStepIcon(step)}</S.SignupFormIcon>
        <S.StepTitle active={true}>{currentTitle}</S.StepTitle>
      </S.SignupFormTitle>
    </S.StepsWrapper>
  );
};
