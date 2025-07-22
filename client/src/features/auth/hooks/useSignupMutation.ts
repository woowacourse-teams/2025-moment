import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { signupUser } from '../api/signupUser';

export const useSignupMutation = () => {
  const navigate = useNavigate();
  return useMutation({
    mutationFn: signupUser,
    onSuccess: () => {
      navigate('/login');
    },
  });
};
