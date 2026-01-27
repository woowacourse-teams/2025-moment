import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';

export const useTransferOwnershipMutation = (groupId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (memberId: number) => transferOwnership(groupId, memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.detail(groupId) });
    },
  });
};

const transferOwnership = async (groupId: string, memberId: number): Promise<void> => {
  await apiClient.post(`/groups/${groupId}/transfer-ownership/${memberId}`);
};
