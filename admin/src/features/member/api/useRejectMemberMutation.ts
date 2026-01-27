import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@shared/api';
import { queryKeys } from '@shared/api/queryKeys';

export const useRejectMemberMutation = (groupId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (memberId: number) => rejectMember(groupId, memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.detail(groupId) });
    },
  });
};

const rejectMember = async (groupId: string, memberId: number): Promise<void> => {
  await apiClient.post(`/groups/${groupId}/members/${memberId}/reject`);
};
