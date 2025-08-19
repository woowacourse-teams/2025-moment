import { useNewPasswordMutation } from '@/features/auth/api/useNewPasswordMutation';
import { NewPassword, NewPasswordErrors } from '@/features/auth/types/newPassword';
import { validatePassword, validateRePassword } from '@/features/auth/utils/validateAuth';
import { useToast } from '@/shared/hooks';
import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router';

export const useNewPassword = () => {
  const { mutate: newPasswordMutation, isPending } = useNewPasswordMutation();
  const [newPasswordFormData, setNewPasswordFormData] = useState<NewPassword>({
    email: '',
    token: '',
    newPassword: '',
    newPasswordCheck: '',
  });
  const [errors, setErrors] = useState<NewPasswordErrors>({
    newPassword: '',
    newPasswordCheck: '',
  });
  const navigate = useNavigate();
  const location = useLocation();
  const { showError } = useToast();
  const queryParams = new URLSearchParams(location.search);
  const email = queryParams.get('email');
  const token = queryParams.get('token');

  useEffect(() => {
    if (!email || !token) {
      showError('인증되지 않은 사용자입니다. 로그인해주세요.');
      navigate('/login', { replace: true });
      return;
    }
    setNewPasswordFormData(prev => ({ ...prev, email, token }));
  }, []);

  const updateNewPasswordForm =
    (field: keyof NewPassword) => (e: React.ChangeEvent<HTMLInputElement>) => {
      const { value } = e.target;
      if (field === 'newPassword') {
        setErrors(prev => ({ ...prev, newPassword: validatePassword(value) }));
      } else if (field === 'newPasswordCheck') {
        setErrors(prev => ({
          ...prev,
          newPasswordCheck: validateRePassword(newPasswordFormData.newPassword, value),
        }));
      }
      setNewPasswordFormData(prev => ({ ...prev, [field]: value }));
    };

  const submitNewPasswordForm = (e: React.FormEvent) => {
    e.preventDefault();
    newPasswordMutation(newPasswordFormData);
  };

  return {
    formData: newPasswordFormData,
    isLoading: isPending,
    errors,
    updateNewPasswordForm,
    submitNewPasswordForm,
  };
};
