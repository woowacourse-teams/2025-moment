import { useFindPasswordMutation } from '@/features/auth/api/useFindPasswordMutation';
import { validateEmail } from '@/features/auth/utils/validateAuth';
import { useState } from 'react';

export const useFindPasswordEmail = () => {
  const [email, setEmail] = useState<string>('');
  const [error, setError] = useState<string>('');
  const { mutate: findNewPassword, isPending } = useFindPasswordMutation();

  const updateEmail = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    const error = validateEmail(value);
    setError(error);
    setEmail(value);
  };

  const submitFindPasswordEmail = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    findNewPassword(email);
  };

  return { email, isLoading: isPending, error, updateEmail, submitFindPasswordEmail };
};
