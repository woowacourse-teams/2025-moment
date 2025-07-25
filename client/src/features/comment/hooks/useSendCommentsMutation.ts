import { queryClient } from '@/app/lib/queryClient';
import { ROUTES } from '@/app/routes/routes';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { sendComments } from '../api/sendComments';

export const useSendCommentsMutation = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: sendComments,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments'] });
      showSuccess('댓글이 성공적으로 등록되었습니다!');
      navigate(ROUTES.TODAY_COMMENT_SUCCESS);
    },
    onError: () => {
      const errorMessage = '댓글 등록에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
