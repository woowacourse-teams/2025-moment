import { useState } from 'react';
import { useCheckEmailCodeMutation } from '../api/useCheckEmailCodeMutation';

export const useCheckEmailCode = () => {
  const [emailCode, setEmailCode] = useState<string>('');
  const {
    mutate,
    isPending: isCheckEmailCodeLoading,
    isError: isCheckEmailCodeError,
    isSuccess: isCheckEmailCodeSuccess,
  } = useCheckEmailCodeMutation();

  const updateEmailCode = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEmailCode(e.target.value);
  };

  const checkEmailCode = (email: string, emailCode: string) => {
    mutate({ email, code: emailCode });
  };

  return {
    emailCode,
    isCheckEmailCodeLoading,
    isCheckEmailCodeError,
    isCheckEmailCodeSuccess,
    updateEmailCode,
    checkEmailCode,
  };
};
