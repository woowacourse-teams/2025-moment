import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { GroupActionResponse } from '../types/group';

export const useDeleteGroupMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (groupId: number | string): Promise<GroupActionResponse> => {
      const response = await api.delete(`/groups/${groupId}`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups'] });
      showSuccess('그룹이 삭제되었습니다.');
    },
    onError: () => {
      showError('그룹 삭제에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
