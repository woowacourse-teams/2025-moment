import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@shared/api";
import { queryKeys } from "@shared/api/queryKeys";

interface DeleteUserRequest {
  reason: string;
}

export const useDeleteUserMutation = (userId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: DeleteUserRequest) => deleteUser(userId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.all });
    },
  });
};

const deleteUser = async (
  userId: string,
  data: DeleteUserRequest,
): Promise<void> => {
  await apiClient.delete(`/users/${userId}`, { data });
};
