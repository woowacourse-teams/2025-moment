import { Step } from '@/shared/types/step';
import { BadgeCheck, Lock, User } from 'lucide-react';
import * as S from './SignupStepsBar.styles';

interface SignupStepBarProps {
  step: Step;
}

export const SignupStepBar = ({ step }: SignupStepBarProps) => {
  const STEP_CONFIG = {
    step1: { number: 1, icon: <Lock />, title: '아이디/비밀번호' },
    step2: { number: 2, icon: <User />, title: '프로필 설정' },
    step3: { number: 3, icon: <BadgeCheck />, title: '완료' },
  } as const;

  const stepConfig = STEP_CONFIG[step] || STEP_CONFIG.step1;

  const currentStepNumber = stepConfig.number;
  const currentTitle = stepConfig.title;

  return (
    <S.StepsWrapper>
      <S.StepsContainer aria-label={`회원가입 진행 단계: 3단계 중 ${currentStepNumber}단계`}>
        <S.StepDot
          active={currentStepNumber === 1}
          completed={currentStepNumber > 1}
          aria-hidden="true"
        />
        <S.StepConnector completed={currentStepNumber > 1} aria-hidden="true" />
        <S.StepDot
          active={currentStepNumber === 2}
          completed={currentStepNumber > 2}
          aria-hidden="true"
        />
        <S.StepConnector completed={currentStepNumber > 2} aria-hidden="true" />
        <S.StepDot
          active={currentStepNumber === 3}
          completed={false}
          aria-hidden="true"
        />
      </S.StepsContainer>
      <S.SignupFormTitle>
        <S.SignupFormIcon aria-hidden="true">{stepConfig.icon}</S.SignupFormIcon>
        <S.StepTitle active={true}>{currentTitle}</S.StepTitle>
      </S.SignupFormTitle>
    </S.StepsWrapper>
  );
};
