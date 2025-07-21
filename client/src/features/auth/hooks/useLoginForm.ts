import { LoginError, LoginFormData } from '@/features/auth/types/login';
import { isLoginFormValid, validateLoginFormData } from '@/features/auth/utils/validateLoginForm';
import { useMemo, useState } from 'react';

export const useLoginForm = () => {
  const [formData, setFormData] = useState<LoginFormData>({
    email: '',
    password: '',
  });

  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<LoginError>({
    email: '',
    password: '',
  });

  const handleChange = (field: keyof LoginFormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [field]: e.target.value }));
  };

  const isDisabled = useMemo(() => {
    return !isLoginFormValid(errors) || isLoading;
  }, [errors, isLoading]);

  const handleBlur = (field: keyof LoginFormData) => (e: React.FocusEvent<HTMLInputElement>) => {
    const validationErrors = validateLoginFormData(formData);
    setErrors(validationErrors);
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // TODO: 로그인 로직 구현
  };

  return {
    formData,
    errors,
    isLoading,
    isDisabled,
    handleChange,
    handleBlur,
    handleSubmit,
  };
};
