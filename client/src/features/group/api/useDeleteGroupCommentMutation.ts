import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';

export const useDeleteGroupCommentMutation = (groupId: number | string, momentId: number) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (commentId: number) => {
      const response = await api.delete(`/v2/groups/${groupId}/comments/${commentId}`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['group', groupId, 'moment', momentId, 'comments'],
      });
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'moments'] });
      showSuccess('코멘트가 삭제되었습니다.');
    },
    onError: () => {
      showError('코멘트 삭제에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
