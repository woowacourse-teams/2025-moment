import { useMutation } from '@tanstack/react-query';
import { passwordChange } from '../api/passwordChange';
import { useNavigate } from 'react-router';
import { useToast } from '@/shared/hooks';

export const usePasswordChangeMutation = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: passwordChange,
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
