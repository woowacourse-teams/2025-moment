import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { track } from '@/shared/lib/ga/track';
import { GroupActionResponse } from '../types/group';

export const useLeaveGroupMutation = () => {

  return useMutation({
    mutationFn: async (groupId: number | string): Promise<GroupActionResponse> => {
      const response = await api.delete(`/groups/${groupId}/leave`);
      return response.data;
    },
    onSuccess: () => {
      track('leave_group', {});
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.all });
      toast.success('그룹에서 탈퇴했습니다.');
    },
    onError: () => {
      toast.error('그룹 탈퇴에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
