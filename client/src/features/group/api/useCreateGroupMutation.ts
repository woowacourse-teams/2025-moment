import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { CreateGroupRequest, GroupActionResponse } from '../types/group';

export const useCreateGroupMutation = () => {

  return useMutation({
    mutationFn: async (data: CreateGroupRequest): Promise<GroupActionResponse> => {
      const response = await api.post('/groups', data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.all });
      toast.success('그룹이 생성되었습니다!');
    },
    onError: () => {
      toast.error('그룹 생성에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
