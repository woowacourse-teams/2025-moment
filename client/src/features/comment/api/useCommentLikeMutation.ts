import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useMutation } from '@tanstack/react-query';

export const useCommentLikeMutation = (groupId: number | string, commentId: number) => {
  return useMutation({
    mutationFn: async () => {
      const response = await api.post(`/groups/${groupId}/comments/${commentId}/like`);
      return response.data;
    },
    onSuccess: () => {
      // Since comment details are usually nested in moments, we might need broader invalidation
      const numericGroupId = Number(groupId);
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId] });
    },
  });
};
