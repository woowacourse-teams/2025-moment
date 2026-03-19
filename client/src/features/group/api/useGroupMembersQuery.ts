import { api } from '@/app/lib/api';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useQuery } from '@tanstack/react-query';
import { GroupMembersResponse } from '../types/group';

export const useGroupMembersQuery = (groupId: number | string) => {
  return useQuery({
    queryKey: queryKeys.group.members(Number(groupId)),
    queryFn: async (): Promise<GroupMembersResponse> => {
      const response = await api.get(`/groups/${groupId}/members`);
      return response.data;
    },
    enabled: !!groupId,
  });
};
