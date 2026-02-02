import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { track } from '@/shared/lib/ga/track';
import { GroupActionResponse } from '../types/group';

export const useLeaveGroupMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (groupId: number | string): Promise<GroupActionResponse> => {
      const response = await api.delete(`/groups/${groupId}/leave`);
      return response.data;
    },
    onSuccess: () => {
      track('leave_group', {});
      queryClient.invalidateQueries({ queryKey: ['groups'] });
      showSuccess('그룹에서 탈퇴했습니다.');
    },
    onError: () => {
      showError('그룹 탈퇴에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
