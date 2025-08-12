import { useToast } from '@/shared/hooks/useToast';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { logoutUser } from '../api/logoutUser';

export const useLogoutMutation = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: logoutUser,
    onSuccess: () => {
      queryClient.clear();
      showSuccess('로그아웃 되었습니다.');
      navigate('/login');
    },
    onError: () => {
      showError('로그아웃에 실패했습니다.');
    },
  });
};
