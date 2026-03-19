import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { getProfile } from '@/features/auth/api/useProfileQuery';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { useNavigate } from 'react-router';

export const useGoogleLoginMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: googleLogin,
    onSuccess: async () => {
      queryClient.setQueryData(queryKeys.auth.checkLogin, true);
      await queryClient.prefetchQuery({ queryKey: queryKeys.auth.profile, queryFn: getProfile });

      toast.success('Google 로그인에 성공했습니다!');
      navigate('/');
    },
    onError: error => {
      if (isAxiosError(error) && error.response?.data.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error('Google 로그인에 실패했습니다. 다시 시도해주세요.');
      }
    },
  });
};

const googleLogin = async (idToken: string): Promise<void> => {
  await api.post('/auth/google/token', { idToken });
};
