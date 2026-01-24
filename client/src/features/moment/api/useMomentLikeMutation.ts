import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useMutation } from '@tanstack/react-query';

export const useMomentLikeMutation = (groupId: number | string, momentId: number) => {
  return useMutation({
    mutationFn: async () => {
      const response = await api.post(`/groups/${groupId}/moments/${momentId}/like`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'moments'] });
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'my-moments'] });
    },
  });
};
