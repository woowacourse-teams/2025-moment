import { api } from '@/app/lib/api';
import { useToast } from '@/shared/hooks';
import { useMutation } from '@tanstack/react-query';

export const useCheckEmail = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: (email: string) => checkEmail(email),
    onSuccess: () => {
      showSuccess('이메일로 인증 코드가 전송되었습니다. 인증 코드를 확인하고 입력해주세요.');
    },
    onError: error => {
      showError(error.message);
    },
  });
};

const checkEmail = async (email: string) => {
  await api.post('/users/auth/email', { email });
};
