import { useQuery } from '@tanstack/react-query';
import { apiClient, type ApiResponse } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';
import type { PendingMemberListData } from '../types/member';

interface Params {
  groupId: string;
  page: number;
  size: number;
}

export const useGroupPendingMembersQuery = ({ groupId, page, size }: Params) => {
  return useQuery({
    queryKey: queryKeys.groups.pendingMembers(groupId, { page, size }),
    queryFn: () => fetchPendingMembers(groupId, page, size),
    enabled: !!groupId,
  });
};

const fetchPendingMembers = async (
  groupId: string,
  page: number,
  size: number,
): Promise<PendingMemberListData> => {
  const response = await apiClient.get<ApiResponse<PendingMemberListData>>(
    `/groups/${groupId}/pending-members`,
    { params: { page, size } },
  );
  return response.data.data;
};
