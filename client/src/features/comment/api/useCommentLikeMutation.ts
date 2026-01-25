import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useMutation } from '@tanstack/react-query';

export const useCommentLikeMutation = (groupId: number | string) => {
  return useMutation({
    mutationFn: (commentId: number) => toggleCommentLike(groupId, commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'comments'] });
    },
  });
};

const toggleCommentLike = async (groupId: number | string, commentId: number) => {
  const response = await api.post(`/groups/${groupId}/comments/${commentId}/like`);
  return response.data;
};
