import { queryClient } from '@/app/lib/queryClient';
import { ROUTES } from '@/app/routes/routes';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { sendComments } from '../api/sendComments';

const COMMENTS_REWARD_POINT = 2;

export const useSendCommentsMutation = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: sendComments,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments'] });
      showSuccess(`${COMMENTS_REWARD_POINT} 포인트를 획득했습니다!`);
      navigate(ROUTES.TODAY_COMMENT_SUCCESS);
    },
    onError: () => {
      const errorMessage = '코멘트 등록에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
