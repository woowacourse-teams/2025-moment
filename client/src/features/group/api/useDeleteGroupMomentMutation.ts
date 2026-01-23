import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';

export const useDeleteGroupMomentMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (momentId: number) => {
      const response = await api.delete(`/v2/groups/${groupId}/moments/${momentId}`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'moments'] });
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'my-moments'] });
      showSuccess('모멘트가 삭제되었습니다.');
    },
    onError: () => {
      showError('모멘트 삭제에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
