import { useQuery } from "@tanstack/react-query";
import { apiClient, type ApiResponse } from "@shared/api";
import { queryKeys } from "@shared/api/queryKeys";
import type { User } from "./useUsersQuery";

export const useUserDetailQuery = (userId: string) => {
  return useQuery({
    queryKey: queryKeys.users.detail(userId),
    queryFn: () => fetchUserDetail(userId),
    enabled: !!userId,
  });
};

const fetchUserDetail = async (userId: string): Promise<User> => {
  const response = await apiClient.get<ApiResponse<User>>(`/users/${userId}`);
  return response.data.data;
};
