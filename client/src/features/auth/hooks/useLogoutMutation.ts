import { useMutation } from '@tanstack/react-query';
import { logoutUser } from '../api/logoutUser';

export const useLogoutMutation = () => {
  return useMutation({
    mutationFn: logoutUser,
    onSuccess: () => {
      alert('로그아웃 되었습니다.');
    },
    onError: () => {
      alert('로그아웃 실패');
    },
  });
};
