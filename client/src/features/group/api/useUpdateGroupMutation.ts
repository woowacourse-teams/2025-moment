import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { UpdateGroupRequest, GroupActionResponse } from '../types/group';

export const useUpdateGroupMutation = (groupId: number | string) => {
  return useMutation({
    mutationFn: async (data: UpdateGroupRequest): Promise<GroupActionResponse> => {
      const response = await api.patch(`/groups/${groupId}`, data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.detail(Number(groupId)) });
      toast.success('그룹 정보가 수정되었습니다!');
    },
    onError: () => {
      toast.error('그룹 정보 수정에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
