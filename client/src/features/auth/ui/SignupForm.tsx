import { useFunnel } from '@/shared/hooks';
import type { Step } from '@/shared/types/step';
import { STEPS } from '@/shared/types/step';
import { Button } from '@/shared/ui/Button';
import { SignupStep1, SignupStep2, SignupStep3, SignupStepBar } from '@/widgets/signup';
import { useState } from 'react';
import * as S from './SignupForm.styles';

export const SignupForm = () => {
  const { Funnel, Step, useStep, beforeStep, nextStep } = useFunnel(STEPS);
  const [password, setPassword] = useState<{ password: string; rePassword: string }>({
    password: '',
    rePassword: '',
  });
  const { step, setStep } = useStep();

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
          <Step name="step1">
            <SignupStep1 password={password} setPassword={setPassword} />
          </Step>
          <Step name="step2">
            <SignupStep2 password={password} />
          </Step>
          <Step name="step3">
            <SignupStep3 />
          </Step>
        </Funnel>
      </S.SignupFormContent>

      <S.ButtonContainer>
        <Button title="이전" onClick={handlePreviousStep} disabled={!beforeStep} />
        <Button title="다음" onClick={handleNextStep} disabled={!nextStep} />
      </S.ButtonContainer>
    </S.SignupFormWrapper>
  );
};
