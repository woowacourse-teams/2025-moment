import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { UpdateGroupRequest, GroupActionResponse } from '../types/group';

export const useUpdateGroupMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (data: UpdateGroupRequest): Promise<GroupActionResponse> => {
      const response = await api.patch(`/v2/groups/${groupId}`, data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups'] });
      queryClient.invalidateQueries({ queryKey: ['group', groupId] });
      showSuccess('그룹 정보가 수정되었습니다!');
    },
    onError: () => {
      showError('그룹 정보 수정에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
