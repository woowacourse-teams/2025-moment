import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
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

  return useMutation({
    mutationFn: (data: Omit<SendCommentsData, 'momentId'>) => sendComments(groupId, momentId, data),
    onSuccess: (_data, variables) => {
      const numericGroupId = Number(groupId);
      queryClient.invalidateQueries({
        queryKey: queryKeys.group.momentComments(numericGroupId, momentId),
      });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.moments(numericGroupId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.commentableMoments.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.comments(numericGroupId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.auth.profile });
      queryClient.invalidateQueries({ queryKey: queryKeys.my.profile });
      queryClient.invalidateQueries({ queryKey: queryKeys.rewardHistory });
      toast.success('코멘트 작성이 완료되었습니다!');

      const length = variables.content?.length ?? 0;
      const length_bucket = length <= 60 ? 's' : length <= 140 ? 'm' : 'l';
      track('submit_comment', { length_bucket });
    },
    onError: () => {
      const errorMessage = '코멘트 등록에 실패했습니다. 다시 시도해주세요.';
      toast.error(errorMessage);
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
