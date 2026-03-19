import { api } from '@/app/lib/api';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useQuery } from '@tanstack/react-query';
import { GroupsResponse } from '../types/group';

export const useGroupsQuery = (options?: { enabled?: boolean }) => {
  return useQuery({
    queryKey: queryKeys.groups.all,
    queryFn: async (): Promise<GroupsResponse> => {
      const response = await api.get('/groups');
      return response.data;
    },
    enabled: options?.enabled ?? true,
  });
};
