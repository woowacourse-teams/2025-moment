import { LoginError, LoginFormData } from '@/features/auth/types/login';
import { useState } from 'react';

export const useLoginForm = () => {
  const [formData, setFormData] = useState<LoginFormData>({
    email: '',
    password: '',
  });
  const [errors, setErrors] = useState<LoginError>({
    email: '',
    password: '',
  });
  const [isLoading, setIsLoading] = useState(false);

  const isDisabled = formData.email === '' || formData.password === '' || isLoading;

  const handleInputChange =
    (field: keyof LoginFormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setFormData({ ...formData, [field]: e.target.value });
    };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const validationErrors = validateLoginFormData(formData);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    // TODO: 로그인 로직 구현
  };

  return {
    formData,
    errors,
    isLoading,
    isDisabled,
    handleInputChange,
    handleSubmit,
  };
};
