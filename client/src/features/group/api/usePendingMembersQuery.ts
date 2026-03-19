import { api } from '@/app/lib/api';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useQuery } from '@tanstack/react-query';
import { PendingMembersResponse } from '../types/group';

export const usePendingMembersQuery = (groupId: number | string) => {
  return useQuery({
    queryKey: queryKeys.group.pending(Number(groupId)),
    queryFn: async (): Promise<PendingMembersResponse> => {
      const response = await api.get(`/groups/${groupId}/pending`);
      return response.data;
    },
    enabled: !!groupId,
  });
};
