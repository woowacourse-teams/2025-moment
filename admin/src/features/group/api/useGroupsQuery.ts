import { useQuery } from "@tanstack/react-query";
import { apiClient, type ApiResponse } from "@shared/api";
import { queryKeys } from "@shared/api/queryKeys";
import type { GroupListData, GroupListParams } from "../types/group";

export const useGroupsQuery = ({
  page,
  size,
  keyword,
  status,
}: GroupListParams) => {
  return useQuery({
    queryKey: queryKeys.groups.list({ page, size, keyword, status }),
    queryFn: () => fetchGroups({ page, size, keyword, status }),
  });
};

const fetchGroups = async ({
  page,
  size,
  keyword,
  status,
}: GroupListParams): Promise<GroupListData> => {
  const params: Record<string, unknown> = { page, size };
  if (keyword) params.keyword = keyword;
  if (status) params.status = status;

  const response = await apiClient.get<ApiResponse<GroupListData>>("/groups", {
    params,
  });
  return response.data.data;
};
