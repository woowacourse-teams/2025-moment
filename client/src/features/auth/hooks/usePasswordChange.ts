import { useCallback, useState } from 'react';
import { PasswordChangeErrors, PasswordChangeRequest } from '../types/passwordChange';
import { usePasswordChangeMutation } from './usePasswordChangeMutation';
import { validatePasswordChangeField, isPasswordChangeFormValid } from '../utils/validateAuth';

export const usePasswordChange = () => {
  const [passwordChangeData, setPasswordChangeData] = useState<PasswordChangeRequest>({
    newPassword: '',
    checkPassword: '',
  });

  const [errors, setErrors] = useState<PasswordChangeErrors>({
    newPassword: '',
    checkPassword: '',
  });

  const { mutateAsync: passwordChange, isPending, isError } = usePasswordChangeMutation();

  const handleChange = useCallback(
    (field: keyof PasswordChangeRequest) => (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = e.target.value;

      setPasswordChangeData(prev => {
        const updatedData = { ...prev, [field]: value };

        const fieldError = validatePasswordChangeField(field, value, updatedData);

        setErrors(prevErrors => {
          const newErrors = {
            ...prevErrors,
            [field]: fieldError,
          };

          if (field === 'newPassword' && updatedData.checkPassword) {
            newErrors.checkPassword = validatePasswordChangeField(
              'checkPassword',
              updatedData.checkPassword,
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

  const handleSubmit = useCallback(async () => {
    try {
      await passwordChange(passwordChangeData);
    } catch (error) {
      console.error('비밀번호 변경 실패:', error);
    }
  }, [passwordChange, passwordChangeData]);

  const isFormValid = isPasswordChangeFormValid(errors);
  const isFormEmpty = !passwordChangeData.newPassword || !passwordChangeData.checkPassword;

  return {
    passwordChangeData,
    errors,
    isLoading: isPending,
    isError,
    handleChange,
    handleSubmit,
    isFormValid,
    isFormEmpty,
    isSubmitDisabled: isFormEmpty || !isFormValid,
  };
};
