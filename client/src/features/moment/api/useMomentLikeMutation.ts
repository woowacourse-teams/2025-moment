import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useMutation } from '@tanstack/react-query';

export const useMomentLikeMutation = (groupId: number | string) => {
  return useMutation({
    mutationFn: (momentId: number) => toggleMomentLike(groupId, momentId),
    onSuccess: (_data, momentId) => {
      const numericGroupId = Number(groupId);
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'moments'] });
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'moment', momentId] });
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'my-moments'] });
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'comments'] });
      queryClient.invalidateQueries({ queryKey: ['commentableMoments', numericGroupId] });
    },
  });
};

const toggleMomentLike = async (groupId: number | string, momentId: number) => {
  const response = await api.post(`/groups/${groupId}/moments/${momentId}/like`);
  return response.data;
};
