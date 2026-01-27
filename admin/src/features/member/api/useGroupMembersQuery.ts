import { useQuery } from '@tanstack/react-query';
import { apiClient, type ApiResponse } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';
import type { MemberListData } from '../types/member';

interface Params {
  groupId: string;
  page: number;
  size: number;
}

export const useGroupMembersQuery = ({ groupId, page, size }: Params) => {
  return useQuery({
    queryKey: queryKeys.groups.members(groupId, { page, size }),
    queryFn: () => fetchGroupMembers(groupId, page, size),
    enabled: !!groupId,
  });
};

const fetchGroupMembers = async (
  groupId: string,
  page: number,
  size: number,
): Promise<MemberListData> => {
  const response = await apiClient.get<ApiResponse<MemberListData>>(
    `/groups/${groupId}/members`,
    { params: { page, size } },
  );
  return response.data.data;
};
