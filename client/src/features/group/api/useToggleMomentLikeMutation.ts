import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useMutation } from '@tanstack/react-query';

export const useToggleMomentLikeMutation = (groupId: number | string) => {
  return useMutation({
    mutationFn: async (momentId: number) => {
      const response = await api.post(`/v2/groups/${groupId}/moments/${momentId}/like`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'moments'] });
    },
  });
};
