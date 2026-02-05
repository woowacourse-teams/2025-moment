import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { track } from '@/shared/lib/ga/track';

export interface SendCommentsData {
  content: string;
  momentId: number;
  imageUrl?: string;
  imageName?: string;
}

export interface SendCommentsResponse {
  status: number;
  data: {
    commentId: number;
    content: string;
    createdAt: string;
  };
}


export const useSendCommentsMutation = (groupId: number | string, momentId: number) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: (data: Omit<SendCommentsData, 'momentId'>) => sendComments(groupId, momentId, data),
    onSuccess: (_data, variables) => {
      const numericGroupId = Number(groupId);
      queryClient.invalidateQueries({
        queryKey: ['group', numericGroupId, 'moment', momentId, 'comments'],
      });
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'moments'] });
      queryClient.invalidateQueries({ queryKey: ['commentableMoments'] });
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'comments'] });
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      queryClient.invalidateQueries({ queryKey: ['my', 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['rewardHistory'] });
      showSuccess('코멘트 작성이 완료되었습니다!');

      const length = variables.content?.length ?? 0;
      const length_bucket = length <= 60 ? 's' : length <= 140 ? 'm' : 'l';
      track('submit_comment', { length_bucket });
    },
    onError: () => {
      const errorMessage = '코멘트 등록에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};

const sendComments = async (
  groupId: number | string,
  momentId: number,
  commentsData: Omit<SendCommentsData, 'momentId'>,
): Promise<SendCommentsResponse> => {
  const response = await api.post(`/groups/${groupId}/moments/${momentId}/comments`, commentsData);
  return response.data;
};
