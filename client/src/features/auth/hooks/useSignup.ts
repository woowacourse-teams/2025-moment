import { useSignupMutation } from '@/features/auth/hooks/useSignupMutation';
import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';
import { isSignupFormValid, validateSignupField } from '@/features/auth/utils/validateAuth';
import { useCallback, useState } from 'react';

export const useSignup = () => {
  const [signupData, setSignupData] = useState<SignupFormData>({
    email: '',
    password: '',
    rePassword: '',
    nickname: '',
  });

  const [errors, setErrors] = useState<SignupErrors>({
    email: '',
    password: '',
    rePassword: '',
    nickname: '',
  });

  const { mutateAsync: signup, isPending, isError } = useSignupMutation();

  const handleChange = useCallback(
    (field: keyof SignupFormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = e.target.value;

      setSignupData(prev => {
        const updatedData = { ...prev, [field]: value };

        const fieldError = validateSignupField(field, value, updatedData);

        setErrors(prevErrors => {
          const newErrors = {
            ...prevErrors,
            [field]: fieldError,
          };

          if (field === 'password' && updatedData.rePassword) {
            newErrors.rePassword = validateSignupField(
              'rePassword',
              updatedData.rePassword,
              updatedData,
            );
          }

          return newErrors;
        });

        return updatedData;
      });
    },
    [],
  );

  const handleClick = useCallback(async () => {
    try {
      await signup(signupData);
    } catch (error) {
      console.error('Signup failed:', error);
    }
  }, [signup, signupData]);

  const updateNickname = useCallback(
    (nickname: string) => {
      setSignupData(prev => ({ ...prev, nickname }));
      const fieldError = validateSignupField('nickname', nickname, { ...signupData, nickname });
      setErrors(prev => ({ ...prev, nickname: fieldError }));
    },
    [signupData],
  );

  const isFormDataEmpty = !signupData.email || !signupData.password || !signupData.rePassword;
  const isFirstStepDisabled = isFormDataEmpty || !isSignupFormValid(errors);

  return {
    signupData,
    signup,
    isLoading: isPending,
    errors,
    isError,
    handleChange,
    handleClick,
    updateNickname,
    isSignupFormValid: isSignupFormValid(errors),
    isFirstStepDisabled,
    isFormDataEmpty,
  };
};
