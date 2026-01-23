import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';
import { GroupsResponse } from '../types/group';

export const useGroupsQuery = () => {
  return useQuery({
    queryKey: ['groups'],
    queryFn: async (): Promise<GroupsResponse> => {
      const response = await api.get('/v2/groups');
      return response.data;
    },
  });
};
