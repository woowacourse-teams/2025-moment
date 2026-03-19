import { api } from '@/app/lib/api';
import { toast } from '@/shared/store/toast';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useCheckEmailMutation = () => {
  return useMutation({
    mutationFn: (email: string) => checkEmail(email),
    onSuccess: () => {
      toast.success('이메일 중복 확인에 성공했습니다.');
    },
    onError: error => {
      if (error instanceof AxiosError) {
        const message = error.response?.data.message;
        toast.error(message);
        return error.response?.data.message;
      }
      const message = toast.error('이메일 중복 확인에 실패했습니다. 다시 시도해주세요.');
      return message;
    },
  });
};

const checkEmail = async (email: string) => {
  const response = await api.post('/auth/email', { email });
  return response.data;
};
