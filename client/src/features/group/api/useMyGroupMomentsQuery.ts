import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';
import { GroupMomentsResponse } from './useGroupMomentsQuery';

export const useMyGroupMomentsQuery = (groupId: number | string) => {
  return useQuery({
    queryKey: ['group', groupId, 'my-moments'],
    queryFn: async (): Promise<GroupMomentsResponse> => {
      const response = await api.get(`/v2/groups/${groupId}/my-moments`);
      return response.data;
    },
    enabled: !!groupId,
  });
};
