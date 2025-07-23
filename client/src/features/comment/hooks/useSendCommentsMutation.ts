import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { sendComments } from '../api/sendComments';
import { queryClient } from '@/app/lib/queryClient';
import { ROUTES } from '@/app/routes/routes';

export const useSendCommentsMutation = () => {
  const navigate = useNavigate();
  return useMutation({
    mutationFn: sendComments,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments'] });
      navigate(ROUTES.TODAY_COMMENT_SUCCESS);
    },
  });
};
