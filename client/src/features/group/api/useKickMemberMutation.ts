import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { GroupActionResponse } from '../types/group';

export const useKickMemberMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (memberId: number): Promise<GroupActionResponse> => {
      const response = await api.delete(`/v2/groups/${groupId}/members/${memberId}`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'members'] });
      showSuccess('멤버가 강퇴되었습니다.');
    },
    onError: () => {
      showError('멤버 강퇴에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
