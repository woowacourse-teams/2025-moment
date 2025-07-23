import { useSignup } from '@/features/auth/hooks/useSignup';
import { isDataEmpty, isSignupFormValid } from '@/features/auth/utils/validateAuth';
import { useFunnel } from '@/shared/hooks/useFunnel';
import type { Step } from '@/shared/types/step';
import { STEPS } from '@/shared/types/step';
import { Button } from '@/shared/ui/button/Button';
import { SignupStep1, SignupStep2, SignupStep3, SignupStepBar } from '@/widgets/signup';
import * as S from './SignupForm.styles';

export const SignupForm = () => {
  const { Funnel, Step, useStep, beforeStep, nextStep } = useFunnel(STEPS);

  const { signupData, errors, handleChange, handleClick } = useSignup();
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

  const isDisabled = !isSignupFormValid(errors) || isDataEmpty(signupData);

  return (
    <S.SignupFormWrapper>
      <SignupStepBar step={step as Step} />
      <S.SignupFormContent>
        <Funnel>
          <Step name="step1">
            <SignupStep1
              signupData={signupData}
              errors={errors}
              handleChange={handleChange}
              onNext={!nextStep || isDisabled ? undefined : handleNextStep}
            />
          </Step>
          <Step name="step2">
            <SignupStep2
              signupData={signupData}
              errors={errors}
              handleChange={handleChange}
              onNext={!nextStep || isDisabled ? undefined : handleNextStep}
            />
          </Step>
          <Step name="step3">
            <SignupStep3 signupData={signupData} handleClick={handleClick} onEnter={handleClick} />
          </Step>
        </Funnel>
      </S.SignupFormContent>

      <S.ButtonContainer>
        <Button title="이전" onClick={handlePreviousStep} disabled={!beforeStep || isDisabled} />
        <Button title="다음" onClick={handleNextStep} disabled={!nextStep || isDisabled} />
      </S.ButtonContainer>
    </S.SignupFormWrapper>
  );
};
