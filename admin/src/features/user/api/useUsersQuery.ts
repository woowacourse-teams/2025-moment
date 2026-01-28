import { useQuery } from "@tanstack/react-query";
import { apiClient, type ApiResponse } from "@shared/api";
import { queryKeys } from "@shared/api/queryKeys";

export type ProviderType = "EMAIL" | "GOOGLE" | "KAKAO" | "APPLE";

export interface User {
  id: number;
  email: string;
  nickname: string;
  providerType: ProviderType;
  createdAt: string;
  deletedAt: string | null;
}

export interface UserListData {
  content: User[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

interface UserListParams {
  page: number;
  size: number;
}

export const useUsersQuery = ({ page, size }: UserListParams) => {
  return useQuery({
    queryKey: queryKeys.users.list({ page, size }),
    queryFn: () => fetchUsers({ page, size }),
  });
};

const fetchUsers = async ({
  page,
  size,
}: UserListParams): Promise<UserListData> => {
  const response = await apiClient.get<ApiResponse<UserListData>>("/users", {
    params: { page, size },
  });
  return response.data.data;
};
