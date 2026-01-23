import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { track } from '@/shared/lib/ga/track';

const MOMENTS_REWARD_POINT = 5;

interface SendMomentsData {
  content: string;
  tagNames: string[];
  imageUrl?: string;
  imageName?: string;
}

export const useMomentsMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: (data: SendMomentsData) => sendMoments(groupId, data),
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'moments'] });
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'my-moments'] });
      queryClient.invalidateQueries({ queryKey: ['momentWritingStatus'] });
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      queryClient.invalidateQueries({ queryKey: ['my', 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['rewardHistory'] });
      showSuccess(`별조각 ${MOMENTS_REWARD_POINT} 개를 획득했습니다!`);

      const length = variables.content?.length ?? 0;
      const content_length_bucket = length <= 60 ? 's' : length <= 140 ? 'm' : 'l';
      const has_media = Boolean(variables.imageUrl && variables.imageName);
      const mood_tag = variables.tagNames?.[0];

      const momentId = data?.data?.id ?? data?.data?.momentId ?? data?.id ?? data?.momentId;

      track('publish_moment', {
        item_id: momentId ?? '',
        has_media,
        content_length_bucket,
        ...(mood_tag ? { mood_tag } : {}),
      });
    },
    onError: () => {
      const errorMessage = '모멘트 등록에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};

const sendMoments = async (groupId: number | string, data: SendMomentsData) => {
  const payload: { content: string; tagNames: string[]; imageUrl?: string; imageName?: string } = {
    content: data.content,
    tagNames: data.tagNames,
  };

  if (data.imageUrl && data.imageName) {
    payload.imageUrl = data.imageUrl;
    payload.imageName = data.imageName;
  }

  const response = await api.post(`/groups/${groupId}/moments`, payload);
  return response.data;
};
