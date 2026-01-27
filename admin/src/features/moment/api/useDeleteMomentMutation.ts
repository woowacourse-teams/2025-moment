import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';

export const useDeleteMomentMutation = (groupId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (momentId: number) => deleteMoment(groupId, momentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.detail(groupId) });
    },
  });
};

const deleteMoment = async (groupId: string, momentId: number): Promise<void> => {
  await apiClient.delete(`/groups/${groupId}/moments/${momentId}`);
};
