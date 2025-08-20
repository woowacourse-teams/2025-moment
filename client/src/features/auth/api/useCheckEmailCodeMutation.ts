import { api } from '@/app/lib/api';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';

export const useCheckEmailCodeMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: ({ email, code }: { email: string; code: string }) => checkEmailCode(email, code),
    onSuccess: () => {
      showSuccess('인증코드가 확인되었습니다.');
    },
    onError: () => {
      showError('인증코드가 일치하지 않습니다. 다시 시도해주세요.');
    },
  });
};

const checkEmailCode = async (email: string, code: string) => {
  await api.post('/auth/email/verify', {
    email,
    code,
  });
};
