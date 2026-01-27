import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';

interface DeleteGroupRequest {
  reason: string;
}

export const useDeleteGroupMutation = (groupId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: DeleteGroupRequest) => deleteGroup(groupId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.all });
    },
  });
};

const deleteGroup = async (groupId: string, data: DeleteGroupRequest): Promise<void> => {
  await apiClient.delete(`/groups/${groupId}`, { data });
};
