import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { JoinGroupRequest, GroupActionResponse } from '../types/group';

export const useJoinGroupMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (data: JoinGroupRequest): Promise<GroupActionResponse> => {
      const response = await api.post('/groups/join', data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.all });
      showSuccess('그룹 가입 신청이 완료되었습니다!');
    },
    onError: () => {
      showError('그룹 가입에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
