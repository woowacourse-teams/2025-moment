import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { GroupActionResponse } from '../types/group';

export const useRejectMemberMutation = (groupId: number | string) => {

  return useMutation({
    mutationFn: async (memberId: number): Promise<GroupActionResponse> => {
      const response = await api.post(`/groups/${groupId}/members/${memberId}/reject`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.group.pending(Number(groupId)) });
      toast.success('가입 요청이 거절되었습니다.');
    },
    onError: () => {
      toast.error('가입 거절에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
