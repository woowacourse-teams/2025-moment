import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { GroupActionResponse } from '../types/group';

export const useApproveMemberMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (memberId: number): Promise<GroupActionResponse> => {
      const response = await api.post(`/groups/${groupId}/members/${memberId}/approve`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'pending'] });
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'members'] });
      showSuccess('멤버가 승인되었습니다!');
    },
    onError: () => {
      showError('멤버 승인에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
