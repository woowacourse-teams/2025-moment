import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useMutation } from '@tanstack/react-query';

export const useToggleCommentLikeMutation = (groupId: number | string, momentId: number) => {
  return useMutation({
    mutationFn: async (commentId: number) => {
      const response = await api.post(`/v2/groups/${groupId}/comments/${commentId}/like`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['group', groupId, 'moment', momentId, 'comments'],
      });
    },
  });
};
