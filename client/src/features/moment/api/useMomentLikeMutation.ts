import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation, InfiniteData } from '@tanstack/react-query';
import { track } from '@/shared/lib/ga/track';
import type { MomentsResponse } from '../types/moments';

function toggleMomentLikeInPages(
  old: InfiniteData<MomentsResponse> | undefined,
  momentId: number,
): InfiniteData<MomentsResponse> | undefined {
  if (!old) return old;
  return {
    ...old,
    pages: old.pages.map(page => ({
      ...page,
      data: {
        ...page.data,
        moments: page.data.moments.map(moment =>
          moment.momentId === momentId || moment.id === momentId
            ? {
                ...moment,
                hasLiked: !moment.hasLiked,
                likeCount: moment.hasLiked ? moment.likeCount - 1 : moment.likeCount + 1,
              }
            : moment,
        ),
      },
    })),
  };
}

export const useMomentLikeMutation = (groupId: number | string) => {
  const numericGroupId = Number(groupId);

  return useMutation({
    mutationFn: (momentId: number) => toggleMomentLike(groupId, momentId),
    onMutate: async (momentId: number) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.group.myMoments(numericGroupId) });
      await queryClient.cancelQueries({ queryKey: queryKeys.group.momentsUnread(numericGroupId) });

      const previousMyMoments = queryClient.getQueryData<InfiniteData<MomentsResponse>>(
        queryKeys.group.myMoments(numericGroupId),
      );
      const previousUnreadMoments = queryClient.getQueryData<InfiniteData<MomentsResponse>>(
        queryKeys.group.momentsUnread(numericGroupId),
      );

      queryClient.setQueryData<InfiniteData<MomentsResponse>>(
        queryKeys.group.myMoments(numericGroupId),
        old => toggleMomentLikeInPages(old, momentId),
      );
      queryClient.setQueryData<InfiniteData<MomentsResponse>>(
        queryKeys.group.momentsUnread(numericGroupId),
        old => toggleMomentLikeInPages(old, momentId),
      );

      return { previousMyMoments, previousUnreadMoments };
    },
    onError: (_error, _momentId, context) => {
      if (context?.previousMyMoments) {
        queryClient.setQueryData(
          queryKeys.group.myMoments(numericGroupId),
          context.previousMyMoments,
        );
      }
      if (context?.previousUnreadMoments) {
        queryClient.setQueryData(
          queryKeys.group.momentsUnread(numericGroupId),
          context.previousUnreadMoments,
        );
      }
    },
    onSettled: (_data, _error, momentId) => {
      track('give_likes', { item_type: 'moment' });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.moments(numericGroupId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.moment(numericGroupId, momentId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.myMoments(numericGroupId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.comments(numericGroupId) });
      queryClient.invalidateQueries({
        queryKey: queryKeys.commentableMoments.byGroup(numericGroupId),
      });
    },
  });
};

const toggleMomentLike = async (groupId: number | string, momentId: number) => {
  const response = await api.post(`/groups/${groupId}/moments/${momentId}/like`);
  return response.data;
};
