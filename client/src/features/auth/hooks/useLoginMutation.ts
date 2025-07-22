import { queryClient } from '@/app/lib/queryClient';
import { loginUser } from '@/features/auth/api/loginUser';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';

export const useLoginMutation = () => {
  const navigate = useNavigate();
  return useMutation({
    mutationFn: loginUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      navigate('/');
    },
  });
};
