import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@shared/api";
import { queryKeys } from "@shared/api/queryKeys";
import type { UpdateGroupRequest } from "../types/group";

export const useUpdateGroupMutation = (groupId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateGroupRequest) => updateGroup(groupId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.all });
    },
  });
};

const updateGroup = async (
  groupId: string,
  data: UpdateGroupRequest,
): Promise<void> => {
  await apiClient.put(`/groups/${groupId}`, data);
};
