import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { getProfile } from '@/features/auth/api/useProfileQuery';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { LoginFormData, LoginResponse } from '../types/login';

export const useLoginMutation = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: loginUser,
    onSuccess: async () => {
      queryClient.setQueryData(['checkIfLoggedIn'], true);
      await queryClient.prefetchQuery({ queryKey: ['profile'], queryFn: getProfile });
      showSuccess('로그인에 성공했습니다!');
      navigate('/');
    },
    onError: () => {
      showError('로그인에 실패했습니다. 다시 시도해주세요.');
    },
  });
};

const loginUser = async (loginData: LoginFormData): Promise<LoginResponse> => {
  const response = await api.post('/auth/login', loginData);
  return response.data;
};
