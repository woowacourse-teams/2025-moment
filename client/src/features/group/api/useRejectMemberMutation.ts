import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { GroupActionResponse } from '../types/group';

export const useRejectMemberMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (memberId: number): Promise<GroupActionResponse> => {
      const response = await api.post(`/groups/${groupId}/members/${memberId}/reject`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.group.pending(Number(groupId)) });
      showSuccess('가입 요청이 거절되었습니다.');
    },
    onError: () => {
      showError('가입 거절에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
