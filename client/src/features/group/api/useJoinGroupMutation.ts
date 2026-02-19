import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { JoinGroupRequest, GroupActionResponse } from '../types/group';

export const useJoinGroupMutation = () => {
  return useMutation({
    mutationFn: async (data: JoinGroupRequest): Promise<GroupActionResponse> => {
      const response = await api.post('/groups/join', data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.all });
    },
    onError: () => {
      toast.error('그룹 가입에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
