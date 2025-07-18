import type { Step } from '@/features/auth/types/step';
import { STEPS } from '@/features/auth/types/step';
import { useFunnel } from '@/shared/hooks';
import { Button } from '@/shared/ui/Button';
import { SignupStep1, SignupStep2, SignupStep3, SignupStepBar } from '@/widgets/signup';
import { useMemo, useState } from 'react';
import * as S from './SignupForm.styles';

export const SignupForm = () => {
  const { Funnel, Step: FunnelStep, useStep } = useFunnel(STEPS);
  const [password, setPassword] = useState<{ password: string; rePassword: string }>(
    {
      password: '',
      rePassword: '',
    }
  );
  const { step, setStep } = useStep();

  const currentStepIndex = useMemo(() => STEPS.indexOf(step as Step), [step]);

  const beforeStep = useMemo(() => {
    return currentStepIndex > 0 ? (STEPS[currentStepIndex - 1] as Step) : null;
  }, [currentStepIndex]);

  const nextStep = useMemo(() => {
    return currentStepIndex < STEPS.length - 1 ? (STEPS[currentStepIndex + 1] as Step) : null;
  }, [currentStepIndex]);

  const handlePreviousStep = () => {
    if (beforeStep) {
      setStep(beforeStep);
    }
  };

  const handleNextStep = () => {
    if (nextStep) {
      setStep(nextStep);
    }
  };


  return (
    <S.SignupFormWrapper>
      <SignupStepBar step={step as Step} />
      <S.SignupFormContent>
        <Funnel>
          <FunnelStep name="step1">
            <SignupStep1 password={password} setPassword={setPassword} />
          </FunnelStep>
          <FunnelStep name="step2">
            <SignupStep2 password={password} />
          </FunnelStep>
          <FunnelStep name="step3">
            <SignupStep3 />
          </FunnelStep>
        </Funnel>
      </S.SignupFormContent>

      <S.ButtonContainer>
        <Button title="이전" onClick={handlePreviousStep} disabled={!beforeStep} />
        <Button title="다음" onClick={handleNextStep} disabled={!nextStep} />{' '}
      </S.ButtonContainer>
    </S.SignupFormWrapper>
  );
};
