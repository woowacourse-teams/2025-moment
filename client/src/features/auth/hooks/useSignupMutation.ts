import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { signupUser } from '../api/signupUser';

export const useSignupMutation = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: signupUser,
    onSuccess: () => {
      showSuccess('회원가입이 완료되었습니다! 로그인해주세요.');
      navigate('/login');
    },
    onError: () => {
      const errorMessage = '회원가입에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
