import { useQuery } from "@tanstack/react-query";
import { apiClient, type ApiResponse } from "@shared/api";
import { queryKeys } from "@shared/api/queryKeys";
import type { GroupMomentListData } from "../types/moment";

interface Params {
  groupId: string;
  page: number;
  size: number;
}

export const useGroupMomentsQuery = ({ groupId, page, size }: Params) => {
  return useQuery({
    queryKey: queryKeys.groups.moments(groupId, { page, size }),
    queryFn: () => fetchGroupMoments(groupId, page, size),
    enabled: !!groupId,
  });
};

const fetchGroupMoments = async (
  groupId: string,
  page: number,
  size: number,
): Promise<GroupMomentListData> => {
  const response = await apiClient.get<ApiResponse<GroupMomentListData>>(
    `/groups/${groupId}/moments`,
    { params: { page, size } },
  );
  return response.data.data;
};
