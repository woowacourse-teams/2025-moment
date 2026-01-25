import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';

export const useDeleteCommentMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: (commentId: number) => deleteComment(groupId, commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'comments'] });
      showSuccess('코멘트가 삭제되었습니다.');
    },
    onError: () => {
      showError('코멘트 삭제에 실패했습니다.');
    },
  });
};

const deleteComment = async (groupId: number | string, commentId: number) => {
  const response = await api.delete(`/groups/${groupId}/comments/${commentId}`);
  return response.data;
};
