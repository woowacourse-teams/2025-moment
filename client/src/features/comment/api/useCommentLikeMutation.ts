import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useMutation } from '@tanstack/react-query';

export const useCommentLikeMutation = (groupId: number | string) => {
  return useMutation({
    mutationFn: (commentId: number) => toggleCommentLike(groupId, commentId),
    onSuccess: () => {
      const numericGroupId = Number(groupId);
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'comments'] });
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'my-moments'] });
    },
  });
};

const toggleCommentLike = async (groupId: number | string, commentId: number) => {
  const response = await api.post(`/groups/${groupId}/comments/${commentId}/like`);
  return response.data;
};
