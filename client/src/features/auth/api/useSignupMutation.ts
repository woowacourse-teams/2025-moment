import { api } from '@/app/lib/api';
import { toast } from '@/shared/store/toast';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { SignupRequest, SignupResponse } from '../types/signup';

export const useSignupMutation = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: signupUser,
    onSuccess: () => {
      toast.success('회원가입이 완료되었습니다! 로그인해주세요.');
      navigate('/login');
    },
    onError: () => {
      const errorMessage = '회원가입에 실패했습니다. 다시 시도해주세요.';
      toast.error(errorMessage);
    },
  });
};

const signupUser = async (signupData: SignupRequest): Promise<SignupResponse> => {
  const response = await api.post('/users/signup', signupData);
  return response.data;
};
