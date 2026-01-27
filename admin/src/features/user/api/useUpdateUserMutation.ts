import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@shared/api";
import { queryKeys } from "@shared/api/queryKeys";

interface UpdateUserRequest {
  nickname: string;
}

export const useUpdateUserMutation = (userId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateUserRequest) => updateUser(userId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.all });
    },
  });
};

const updateUser = async (
  userId: string,
  data: UpdateUserRequest,
): Promise<void> => {
  await apiClient.patch(`/users/${userId}`, data);
};
