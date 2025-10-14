import { storageService } from '@/shared/utils/storageService';
import { useEffect, useState } from 'react';
import { useCheckEmailCodeMutation } from '../api/useCheckEmailCodeMutation';

const EMAIL_CODE_KEY = 'signup_email_code';
const EMAIL_CODE_SUCCESS_KEY = 'signup_email_code_success';

export const useCheckEmailCode = () => {
  const [emailCode, setEmailCode] = useState<string>(() => {
    return storageService.session.get<string>(EMAIL_CODE_KEY) || '';
  });

  const [wasSuccessful, setWasSuccessful] = useState<boolean>(() => {
    return storageService.session.get<boolean>(EMAIL_CODE_SUCCESS_KEY) || false;
  });

  const {
    mutate,
    isPending: isCheckEmailCodeLoading,
    isError: isCheckEmailCodeError,
    isSuccess: isCheckEmailCodeSuccess,
  } = useCheckEmailCodeMutation();

  useEffect(() => {
    storageService.session.set(EMAIL_CODE_KEY, emailCode);
  }, [emailCode]);

  useEffect(() => {
    if (isCheckEmailCodeSuccess) {
      setWasSuccessful(true);
      storageService.session.set(EMAIL_CODE_SUCCESS_KEY, true);
    }
  }, [isCheckEmailCodeSuccess]);

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
    isCheckEmailCodeSuccess: isCheckEmailCodeSuccess || wasSuccessful,
    updateEmailCode,
    checkEmailCode,
  };
};
