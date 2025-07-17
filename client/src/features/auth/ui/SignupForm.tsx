import { useFunnel } from '@/shared/hooks';
import { SignupStep1, SignupStep2, SignupStep3 } from '@/widgets/signup';
import * as S from './SignupForm.styles';

const STEPS = ['step1', 'step2', 'step3'] as const;

export const SignupForm = () => {
  const { Funnel, Step, useStep } = useFunnel(STEPS);

  return (
    <S.SignupFormWrapper>
      <Funnel>
        <Step name="step1">
          <SignupStep1 useStep={useStep} />
        </Step>
        <Step name="step2">
          <SignupStep2 useStep={useStep} />
        </Step>
        <Step name="step3">
          <SignupStep3 useStep={useStep} />
        </Step>
      </Funnel>
    </S.SignupFormWrapper>
  );
};
