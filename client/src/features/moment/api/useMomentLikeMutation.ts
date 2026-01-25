import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useMutation } from '@tanstack/react-query';

export const useMomentLikeMutation = (groupId: number | string) => {
  return useMutation({
    mutationFn: (momentId: number) => toggleMomentLike(groupId, momentId),
    onSuccess: (_data, momentId) => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'moments'] });
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'moment', momentId] });
    },
  });
};

const toggleMomentLike = async (groupId: number | string, momentId: number) => {
  const response = await api.post(`/groups/${groupId}/moments/${momentId}/like`);
  return response.data;
};
