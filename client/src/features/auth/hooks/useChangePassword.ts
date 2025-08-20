import { useCallback, useState } from 'react';
import { useChangePasswordMutation } from '../api/useChangePasswordMutation';
import { ChangePasswordErrors, ChangePasswordRequest } from '../types/changePassword';
import { isChangePasswordFormValid, validateChangePasswordField } from '../utils/validateAuth';

export const useChangePassword = () => {
  const [changePasswordData, setChangePasswordData] = useState<ChangePasswordRequest>({
    newPassword: '',
    checkedPassword: '',
  });

  const [errors, setErrors] = useState<ChangePasswordErrors>({
    newPassword: '',
    checkedPassword: '',
  });

  const { mutateAsync: changePassword, isPending, isError } = useChangePasswordMutation();

  const handleChange = useCallback(
    (field: keyof ChangePasswordRequest) => (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = e.target.value;

      setChangePasswordData(prev => {
        const updatedData = { ...prev, [field]: value };

        const fieldError = validateChangePasswordField(field, value, updatedData);

        setErrors(prevErrors => {
          const newErrors = {
            ...prevErrors,
            [field]: fieldError,
          };

          if (field === 'newPassword' && updatedData.checkedPassword) {
            newErrors.checkedPassword = validateChangePasswordField(
              'checkedPassword',
              updatedData.checkedPassword,
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
      await changePassword(changePasswordData);
    } catch (error) {
      console.error('비밀번호 변경 실패:', error);
    }
  }, [changePassword, changePasswordData]);

  const isFormValid = isChangePasswordFormValid(errors);
  const isFormEmpty = !changePasswordData.newPassword || !changePasswordData.checkedPassword;

  return {
    changePasswordData,
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
