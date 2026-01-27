import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';

export const useApproveMemberMutation = (groupId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (memberId: number) => approveMember(groupId, memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.detail(groupId) });
    },
  });
};

const approveMember = async (groupId: string, memberId: number): Promise<void> => {
  await apiClient.post(`/groups/${groupId}/members/${memberId}/approve`);
};
