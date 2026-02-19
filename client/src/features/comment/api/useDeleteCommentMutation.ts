import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';

export const useDeleteCommentMutation = (groupId: number | string) => {

  return useMutation({
    mutationFn: (commentId: number) => deleteComment(groupId, commentId),
    onSuccess: () => {
      const numericGroupId = Number(groupId);
      queryClient.invalidateQueries({ queryKey: queryKeys.group.comments(numericGroupId) });
      toast.success('코멘트가 삭제되었습니다.');
    },
    onError: () => {
      toast.error('코멘트 삭제에 실패했습니다.');
    },
  });
};

const deleteComment = async (groupId: number | string, commentId: number) => {
  const response = await api.delete(`/groups/${groupId}/comments/${commentId}`);
  return response.data;
};
