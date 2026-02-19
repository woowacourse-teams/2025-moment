import { api } from '@/app/lib/api';
import { toast } from '@/shared/store/toast';
import { useMutation } from '@tanstack/react-query';

export const useFindPasswordMutation = () => {

  return useMutation({
    mutationFn: (email: string) => verifyNewPassword(email),
    onSuccess: () => {
      toast.success('이메일로 인증 링크가 전송되었습니다. 이메일을 확인해주세요.');
    },
    onError: () => {
      toast.error('이메일 인증에 실패했습니다. 다시 시도해주세요.');
    },
  });
};

const verifyNewPassword = async (email: string) => {
  const response = await api.post('/auth/email/password', {
    email,
  });
  return response.data;
};
