import { storageService } from '@/shared/utils/storageService';
import { useEffect, useState } from 'react';
import { useCheckEmailMutation } from '../api/useCheckEmailMutation';

const EMAIL_CHECK_SUCCESS_KEY = 'signup_email_check_success';

export const useCheckEmailWithStorage = () => {
  const [wasEmailChecked, setWasEmailChecked] = useState<boolean>(() => {
    return storageService.session.get<boolean>(EMAIL_CHECK_SUCCESS_KEY) || false;
  });

  const { mutate, isPending, isError, error, isSuccess } = useCheckEmailMutation();

  useEffect(() => {
    if (isSuccess) {
      setWasEmailChecked(true);
      storageService.session.set(EMAIL_CHECK_SUCCESS_KEY, true);
    }
  }, [isSuccess]);

  return {
    mutate,
    isPending,
    isError,
    error,
    isSuccess: isSuccess || wasEmailChecked,
  };
};
