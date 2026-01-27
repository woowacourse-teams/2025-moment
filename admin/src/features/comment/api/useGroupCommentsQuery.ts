import { useQuery } from '@tanstack/react-query';
import { apiClient, type ApiResponse } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';
import type { GroupCommentListData } from '../types/comment';

interface Params {
  groupId: string;
  momentId: string;
  page: number;
  size: number;
}

export const useGroupCommentsQuery = ({ groupId, momentId, page, size }: Params) => {
  return useQuery({
    queryKey: queryKeys.groups.comments(groupId, momentId, { page, size }),
    queryFn: () => fetchGroupComments(groupId, momentId, page, size),
    enabled: !!groupId && !!momentId,
  });
};

const fetchGroupComments = async (
  groupId: string,
  momentId: string,
  page: number,
  size: number,
): Promise<GroupCommentListData> => {
  const response = await apiClient.get<ApiResponse<GroupCommentListData>>(
    `/groups/${groupId}/moments/${momentId}/comments`,
    { params: { page, size } },
  );
  return response.data.data;
};
