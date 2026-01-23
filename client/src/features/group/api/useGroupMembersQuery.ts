import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';
import { GroupMembersResponse } from '../types/group';

export const useGroupMembersQuery = (groupId: number | string) => {
  return useQuery({
    queryKey: ['group', groupId, 'members'],
    queryFn: async (): Promise<GroupMembersResponse> => {
      const response = await api.get(`/groups/${groupId}/members`);
      return response.data;
    },
    enabled: !!groupId,
  });
};
