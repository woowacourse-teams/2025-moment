import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { track } from '@/shared/lib/ga/track';

interface SendMomentsData {
  content: string;
  imageUrl?: string;
  imageName?: string;
}

export const useMomentsMutation = (groupId: number | string) => {
  const { showError, showSuccess } = useToast();

  return useMutation({
    mutationFn: (data: SendMomentsData) => sendMoments(groupId, data),
    onSuccess: (_data, variables) => {
      const numericGroupId = Number(groupId);
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'moments'] });
      queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'my-moments'] });
      queryClient.invalidateQueries({ queryKey: ['momentWritingStatus'] });
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      queryClient.invalidateQueries({ queryKey: ['my', 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['rewardHistory'] });

      const length = variables.content?.length ?? 0;
      const content_length_bucket = length <= 60 ? 's' : length <= 140 ? 'm' : 'l';
      const has_media = Boolean(variables.imageUrl && variables.imageName);

      track('publish_moment', { has_media, content_length_bucket });
      showSuccess('모멘트 작성이 완료되었습니다!');
    },
    onError: () => {
      const errorMessage = '모멘트 등록에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};

const sendMoments = async (groupId: number | string, data: SendMomentsData) => {
  const payload: { content: string; imageUrl?: string; imageName?: string } = {
    content: data.content,
  };

  if (data.imageUrl && data.imageName) {
    payload.imageUrl = data.imageUrl;
    payload.imageName = data.imageName;
  }

  const response = await api.post(`/groups/${groupId}/moments`, payload);
  return response.data;
};
