import { useQuery } from "@tanstack/react-query";
import { apiClient, type ApiResponse } from "@shared/api";
import { queryKeys } from "@shared/api/queryKeys";
import type { GroupStats } from "../types/group";

export const useGroupStatsQuery = () => {
  return useQuery({
    queryKey: queryKeys.groups.stats(),
    queryFn: fetchGroupStats,
  });
};

const fetchGroupStats = async (): Promise<GroupStats> => {
  const response =
    await apiClient.get<ApiResponse<GroupStats>>("/groups/stats");
  return response.data.data;
};
