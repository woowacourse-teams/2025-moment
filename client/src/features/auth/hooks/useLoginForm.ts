import { LoginError, LoginFormData } from '@/features/auth/types/login';
import { useEffect, useState } from 'react';

export const useLoginForm = () => {
  const [formData, setFormData] = useState<LoginFormData>({
    email: '',
    password: '',
  });
  const [error, setError] = useState<LoginError>({});
  const [isLoading, setIsLoading] = useState(false);
  const [isDisabled, setIsDisabled] = useState(true);

  useEffect(() => {
    setIsDisabled(formData.email === '' || formData.password === '' || isLoading);
  }, [formData, isLoading]);

  const handleInputChange =
    (field: keyof LoginFormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setFormData({ ...formData, [field]: e.target.value });
    };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // TODO: 로그인 로직 구현
  };

  return {
    formData,
    error,
    isLoading,
    isDisabled,
    handleInputChange,
    handleSubmit,
  };
};
