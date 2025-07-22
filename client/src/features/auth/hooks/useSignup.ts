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

  const { mutateAsync: signup, isPending, error, isError } = useSignupMutation();

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
      // 추후 UI 레벨에서 에러 처리 -> 여기서 토스트, 에러 메시지 등 표시 필요
      console.error('Signup failed:', error);
    }
  }, [signup, signupData]);

  return {
    signupData,
    signup,
    isLoading: isPending,
    errors,
    isError,
    handleChange,
    handleClick,
    isSignupFormValid: isSignupFormValid(errors),
  };
};
