import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';

export const useKickMemberMutation = (groupId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (memberId: number) => kickMember(groupId, memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.detail(groupId) });
    },
  });
};

const kickMember = async (groupId: string, memberId: number): Promise<void> => {
  await apiClient.delete(`/groups/${groupId}/members/${memberId}`);
};
