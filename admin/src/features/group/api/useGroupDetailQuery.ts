import { useQuery } from '@tanstack/react-query';
import { apiClient, type ApiResponse } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';
import type { GroupDetail } from '../types/group';

export const useGroupDetailQuery = (groupId: string) => {
  return useQuery({
    queryKey: queryKeys.groups.detail(groupId),
    queryFn: () => fetchGroupDetail(groupId),
    enabled: !!groupId,
  });
};

const fetchGroupDetail = async (groupId: string): Promise<GroupDetail> => {
  const response = await apiClient.get<ApiResponse<GroupDetail>>(`/groups/${groupId}`);
  return response.data.data;
};
