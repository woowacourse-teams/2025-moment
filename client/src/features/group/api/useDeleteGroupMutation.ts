import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { GroupActionResponse } from '../types/group';

export const useDeleteGroupMutation = () => {
  return useMutation({
    mutationFn: async (groupId: number | string): Promise<GroupActionResponse> => {
      const response = await api.delete(`/groups/${groupId}`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.groups.all });
      toast.success('그룹이 삭제되었습니다.');
    },
    onError: () => {
      toast.error('그룹 삭제에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
