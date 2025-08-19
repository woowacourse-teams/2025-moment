import { api } from '@/app/lib/api';
import { ROUTES } from '@/app/routes/routes';
import type { UpdatePassword } from '@/features/auth/types/updatePassword';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';

export const useNewPassword = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: (data: UpdatePassword) => getNewPassword(data),
    onSuccess: () => {
      showSuccess('비밀번호가 재변경되었습니다.');
      navigate(ROUTES.LOGIN);
    },
    onError: () => {
      showError('비밀번호 변경에 실패했습니다. 다시 시도해주세요.');
    },
  });
};

const getNewPassword = async ({ email, token, newPassword, newPasswordCheck }: UpdatePassword) => {
  const response = await api.post('/auth/email/password/reset', {
    email,
    token,
    newPassword,
    newPasswordCheck,
  });
  return response.data;
};
