import { useLoginMutation } from '@/features/auth/hooks/useLoginMutation';
import { LoginError, LoginFormData } from '@/features/auth/types/login';
import { isLoginFormValid, validateLoginForm } from '@/features/auth/utils/validateAuth';
import { useMemo, useState } from 'react';

export const useLoginForm = () => {
  const [formData, setFormData] = useState<LoginFormData>({
    email: '',
    password: '',
  });

  const [errors, setErrors] = useState<LoginError>({
    email: '',
    password: '',
  });

  const { mutateAsync: login, isPending, error, isError } = useLoginMutation();

  const handleChange = (field: keyof LoginFormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    const newFormData = { ...formData, [field]: newValue };

    setFormData(newFormData);

    const validationErrors = validateLoginForm(newFormData);
    setErrors(validationErrors);
  };

  const isDisabled = useMemo(() => {
    return !isLoginFormValid(errors) || isPending;
  }, [errors, isPending]);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const validationErrors = validateLoginForm(formData);
    setErrors(validationErrors);

    if (isLoginFormValid(validationErrors)) {
      try {
        await login(formData);
      } catch (error) {
        // 추후 UI 레벨에서 에러 처리 -> 여기서 토스트, 에러 메시지 등 표시 필요
        console.error('Login failed:', error);
      }
    }
  };

  return {
    formData,
    errors,
    isLoading: isPending,
    isDisabled,
    handleChange,
    handleSubmit,
    isError,
    error,
  };
};
