import { useMutation, useQueryClient } from '@tanstack/react-query';
import { logoutUser } from '../api/logoutUser';

export const useLogoutMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: logoutUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      alert('로그아웃 되었습니다.');
    },
    onError: () => {
      alert('로그아웃 실패');
    },
  });
};
