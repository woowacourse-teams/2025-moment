import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { UpdateProfileRequest, GroupActionResponse } from '../types/group';

export const useUpdateProfileMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (data: UpdateProfileRequest): Promise<GroupActionResponse> => {
      const response = await api.patch(`/groups/${groupId}/profile`, data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'members'] });
      showSuccess('프로필이 수정되었습니다!');
    },
    onError: () => {
      showError('프로필 수정에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
