import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';

export const useRestoreGroupMutation = (groupId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => restoreGroup(groupId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.all });
    },
  });
};

const restoreGroup = async (groupId: string): Promise<void> => {
  await apiClient.post(`/groups/${groupId}/restore`);
};
