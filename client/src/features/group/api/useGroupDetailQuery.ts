import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';
import { GroupDetailResponse } from '../types/group';

export const useGroupDetailQuery = (groupId: number | string) => {
  return useQuery({
    queryKey: ['group', groupId],
    queryFn: async (): Promise<GroupDetailResponse> => {
      const response = await api.get(`/groups/${groupId}`);
      return response.data;
    },
    enabled: !!groupId && groupId !== 'undefined',
  });
};
