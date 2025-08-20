import { useCheckEmailMutation } from '@/features/auth/api/useCheckEmailMutation';
import { useCheckEmailCode } from '@/features/auth/hooks/useCheckEmailCode';
import { useSignup } from '@/features/auth/hooks/useSignup';
import { useFunnel } from '@/shared/hooks/useFunnel';
import type { Step } from '@/shared/types/step';
import { STEPS } from '@/shared/types/step';
import { Button } from '@/shared/ui/button/Button';
import { SignupStep1, SignupStep2, SignupStep3, SignupStepBar } from '@/widgets/signup';
import { AxiosError } from 'axios';
import * as S from './SignupForm.styles';

export const SignupForm = () => {
  const { Funnel, Step, useStep, beforeStep, nextStep } = useFunnel(STEPS);

  const { signupData, errors, handleChange, handleClick, updateNickname, isFirstStepDisabled } =
    useSignup();
  const { step, setStep } = useStep();

  const {
    mutate: checkEmail,
    isPending: isEmailCheckLoading,
    isError: isEmailCheckError,
    error: emailCheckError,
    isSuccess: isEmailCheckSuccess,
  } = useCheckEmailMutation();
  const {
    emailCode,
    isCheckEmailCodeLoading,
    isCheckEmailCodeError,
    isCheckEmailCodeSuccess,
    updateEmailCode,
    checkEmailCode,
  } = useCheckEmailCode();

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

  const isDisabled =
    (nextStep && isFirstStepDisabled) ||
    !isEmailCheckSuccess ||
    isEmailCheckError ||
    !isEmailCheckSuccess ||
    !isCheckEmailCodeSuccess;

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
              onNext={isDisabled ? undefined : handleNextStep}
              handleCheckEmail={checkEmail}
              emailErrorMessage={
                emailCheckError instanceof AxiosError
                  ? (emailCheckError.response?.data?.message ?? '')
                  : ''
              }
              isEmailChecked={isEmailCheckSuccess}
              isEmailCheckLoading={isEmailCheckLoading}
              emailCode={emailCode}
              isCheckEmailCodeLoading={isCheckEmailCodeLoading}
              isCheckEmailCodeError={isCheckEmailCodeError}
              isCheckEmailCodeSuccess={isCheckEmailCodeSuccess}
              updateEmailCode={updateEmailCode}
              checkEmailCode={checkEmailCode}
            />
          </Step>
          <Step name="step2">
            <SignupStep2
              signupData={signupData}
              onNext={!nextStep ? undefined : handleNextStep}
              updateNickname={updateNickname}
            />
          </Step>
          <Step name="step3">
            <SignupStep3 signupData={signupData} handleClick={handleClick} onEnter={handleClick} />
          </Step>
        </Funnel>
      </S.SignupFormContent>

      <S.ButtonContainer>
        <Button title="이전" onClick={handlePreviousStep} disabled={!beforeStep} />
        <Button title="다음" onClick={handleNextStep} disabled={isDisabled} />
      </S.ButtonContainer>
    </S.SignupFormWrapper>
  );
};
