import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { GroupActionResponse } from '../types/group';

export const useTransferOwnershipMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (memberId: number): Promise<GroupActionResponse> => {
      const response = await api.post(`/v2/groups/${groupId}/transfer/${memberId}`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups'] });
      queryClient.invalidateQueries({ queryKey: ['group', groupId] });
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'members'] });
      showSuccess('그룹 소유권이 이전되었습니다.');
    },
    onError: () => {
      showError('소유권 이전에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
