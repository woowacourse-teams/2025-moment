import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { getProfile } from '@/features/auth/api/useProfileQuery';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { LoginFormData, LoginResponse } from '../types/login';
import { isAxiosError } from 'axios';

export const useLoginMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: loginUser,
    onSuccess: async () => {
      queryClient.setQueryData(queryKeys.auth.checkLogin, true);
      await queryClient.prefetchQuery({ queryKey: queryKeys.auth.profile, queryFn: getProfile });

      toast.success('로그인에 성공했습니다!');
      navigate('/');
    },
    onError: error => {
      if (isAxiosError(error) && error.response?.data.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error('로그인에 실패했습니다. 다시 시도해주세요.');
      }
    },
  });
};

const loginUser = async (loginData: LoginFormData): Promise<LoginResponse> => {
  const response = await api.post('/auth/login', loginData);
  return response.data;
};
