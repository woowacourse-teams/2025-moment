import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';
import { GroupsResponse } from '../types/group';

export const useGroupsQuery = (options?: { enabled?: boolean }) => {
  return useQuery({
    queryKey: ['groups'],
    queryFn: async (): Promise<GroupsResponse> => {
      const response = await api.get('/groups');
      return response.data;
    },
    enabled: options?.enabled ?? true,
  });
};
