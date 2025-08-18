import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { useToast } from '@/shared/hooks';
import { changePassword } from '../api/changePassword';
import { ChangePasswordRequest } from '../types/changePassword';

export const useChangePasswordMutation = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: (data: ChangePasswordRequest) => changePassword(data),
    onSuccess: () => {
      showSuccess('비밀번호가 변경되었습니다.');
      navigate('/login');
    },
    onError: () => {
      const errorMessage = '비밀번호 변경에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
