import { api } from '@/app/lib/api';
import { ROUTES } from '@/app/routes/routes';
import type { NewPassword } from '@/features/auth/types/newPassword';
import { toast } from '@/shared/store/toast';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';

export const useNewPasswordMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: NewPassword) => getNewPassword(data),
    onSuccess: () => {
      toast.success('비밀번호가 재발급되었습니다.');
      navigate(ROUTES.LOGIN);
    },
    onError: () => {
      toast.error('비밀번호 재발급을 실패했습니다. 다시 시도해주세요.');
    },
  });
};

const getNewPassword = async ({ email, token, newPassword, newPasswordCheck }: NewPassword) => {
  const response = await api.post('/auth/email/password/reset', {
    email,
    token,
    newPassword,
    newPasswordCheck,
  });
  return response.data;
};
