import { useSignupMutation } from '@/features/auth/api/useSignupMutation';
import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';
import { isSignupFormValid, validateSignupField } from '@/features/auth/utils/validateAuth';
import { storageService } from '@/shared/utils/storageService';
import { useCallback, useEffect, useState } from 'react';

const SIGNUP_DATA_KEY = 'signup_data';
const EMAIL_CODE_KEY = 'signup_email_code';
const EMAIL_CODE_SUCCESS_KEY = 'signup_email_code_success';
const EMAIL_CHECK_SUCCESS_KEY = 'signup_email_check_success';

const clearSignupStorage = () => {
  storageService.session.removeMultiple([
    SIGNUP_DATA_KEY,
    EMAIL_CODE_KEY,
    EMAIL_CODE_SUCCESS_KEY,
    EMAIL_CHECK_SUCCESS_KEY,
  ]);
};

export const useSignup = () => {
  const [signupData, setSignupData] = useState<SignupFormData>(() => {
    const savedData = storageService.session.get<SignupFormData>(SIGNUP_DATA_KEY);
    return (
      savedData || {
        email: '',
        password: '',
        rePassword: '',
        nickname: '',
      }
    );
  });

  const [errors, setErrors] = useState<SignupErrors>({
    email: '',
    password: '',
    rePassword: '',
    nickname: '',
  });

  useEffect(() => {
    storageService.session.set(SIGNUP_DATA_KEY, signupData);
  }, [signupData]);

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
      clearSignupStorage();
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
