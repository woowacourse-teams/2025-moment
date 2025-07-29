import { queryClient } from '@/app/lib/queryClient';
import { loginUser } from '@/features/auth/api/loginUser';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';

export const useLoginMutation = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: loginUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      showSuccess('로그인에 성공했습니다!');
      navigate('/');
    },
    onError: (error: any) => {
      const errorMessage =
        error?.response?.data?.message || '로그인에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
