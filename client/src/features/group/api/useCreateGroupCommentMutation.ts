import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';

export interface CreateGroupCommentRequest {
  content: string;
  imageUrl?: string;
}

export const useCreateGroupCommentMutation = (groupId: number | string, momentId: number) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (data: CreateGroupCommentRequest) => {
      const response = await api.post(`/v2/groups/${groupId}/moments/${momentId}/comments`, data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['group', groupId, 'moment', momentId, 'comments'],
      });
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'moments'] });
      showSuccess('코멘트가 작성되었습니다!');
    },
    onError: () => {
      showError('코멘트 작성에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
