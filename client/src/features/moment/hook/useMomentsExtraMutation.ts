import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { sendExtraMoments } from '../api/sendExtraMoments';
import { track } from '@/shared/lib/ga/track';

export const useMomentsExtraMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: sendExtraMoments,
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      queryClient.invalidateQueries({ queryKey: ['my', 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['rewardHistory'] });

      showSuccess('추가 모멘트가 성공적으로 등록되었습니다!');

      const length = variables.content?.length ?? 0;
      const content_length_bucket = length <= 60 ? 's' : length <= 140 ? 'm' : 'l';
      const has_media = Boolean(variables.imageUrl && variables.imageName);
      const mood_tag = variables.tagNames?.[0];
      track('publish_moment', {
        item_id: _data.id,
        has_media,
        content_length_bucket,
        ...(mood_tag ? { mood_tag } : {}),
      });
    },
    onError: () => {
      const errorMessage = '추가 모멘트 등록에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
