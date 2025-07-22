import { useSignupMutation } from '@/features/auth/hooks/useSignupMutation';
import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';
import { useState } from 'react';

export const useSignup = () => {
  const [signupData, setSignupData] = useState({
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

  const handleChange =
    (field: keyof SignupFormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setSignupData(prev => ({ ...prev, [field]: e.target.value }));
    };

  const handleClick = async () => {
    try {
      await signup(signupData);
    } catch (error) {
      // 추후 UI 레벨에서 에러 처리 -> 여기서 토스트, 에러 메시지 등 표시 필요
      console.error('Signup failed:', error);
    }
  };

  return {
    signupData,
    signup,
    isLoading: isPending,
    errors,
    isError,
    handleChange,
    handleClick,
  };
};
