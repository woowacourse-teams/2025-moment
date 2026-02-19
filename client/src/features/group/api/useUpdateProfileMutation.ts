import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { UpdateProfileRequest, GroupActionResponse } from '../types/group';

export const useUpdateProfileMutation = (groupId: number | string) => {

  return useMutation({
    mutationFn: async (data: UpdateProfileRequest): Promise<GroupActionResponse> => {
      const response = await api.patch(`/groups/${groupId}/profile`, data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.group.members(Number(groupId)) });
      toast.success('프로필이 수정되었습니다!');
    },
    onError: () => {
      toast.error('프로필 수정에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
