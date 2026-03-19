import { api } from '@/app/lib/api';
import { toast } from '@/shared/store/toast';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { ChangePasswordRequest, ChangePasswordResponse } from '../types/changePassword';

export const useChangePasswordMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: ChangePasswordRequest) => changePassword(data),
    onSuccess: () => {
      toast.success('비밀번호가 변경되었습니다.');
      navigate('/login');
    },
    onError: () => {
      const errorMessage = '비밀번호 변경에 실패했습니다. 다시 시도해주세요.';
      toast.error(errorMessage);
    },
  });
};

export const changePassword = async (
  data: ChangePasswordRequest,
): Promise<ChangePasswordResponse> => {
  const response = await api.post('/me/password', data);
  return response.data;
};
