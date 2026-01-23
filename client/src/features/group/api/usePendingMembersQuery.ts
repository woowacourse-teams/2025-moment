import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';
import { PendingMembersResponse } from '../types/group';

export const usePendingMembersQuery = (groupId: number | string) => {
  return useQuery({
    queryKey: ['group', groupId, 'pending'],
    queryFn: async (): Promise<PendingMembersResponse> => {
      const response = await api.get(`/groups/${groupId}/pending`);
      return response.data;
    },
    enabled: !!groupId,
  });
};
