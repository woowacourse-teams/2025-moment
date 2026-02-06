import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation, InfiniteData } from '@tanstack/react-query';
import { track } from '@/shared/lib/ga/track';
import type { CommentsResponse } from '../types/comments';

function toggleCommentLikeInPages(
  old: InfiniteData<CommentsResponse> | undefined,
  commentId: number,
): InfiniteData<CommentsResponse> | undefined {
  if (!old) return old;
  return {
    ...old,
    pages: old.pages.map(page => ({
      ...page,
      data: {
        ...page.data,
        comments: page.data.comments.map(comment =>
          comment.id === commentId
            ? {
                ...comment,
                hasLiked: !comment.hasLiked,
                likeCount: comment.hasLiked ? comment.likeCount - 1 : comment.likeCount + 1,
              }
            : comment,
        ),
      },
    })),
  };
}

export const useCommentLikeMutation = (groupId: number | string) => {
  const numericGroupId = Number(groupId);

  return useMutation({
    mutationFn: (commentId: number) => toggleCommentLike(groupId, commentId),
    onMutate: async (commentId: number) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.group.comments(numericGroupId) });
      await queryClient.cancelQueries({
        queryKey: queryKeys.group.commentsUnread(numericGroupId),
      });

      const previousComments = queryClient.getQueryData<InfiniteData<CommentsResponse>>(
        queryKeys.group.comments(numericGroupId),
      );
      const previousUnreadComments = queryClient.getQueryData<InfiniteData<CommentsResponse>>(
        queryKeys.group.commentsUnread(numericGroupId),
      );

      queryClient.setQueryData<InfiniteData<CommentsResponse>>(
        queryKeys.group.comments(numericGroupId),
        old => toggleCommentLikeInPages(old, commentId),
      );
      queryClient.setQueryData<InfiniteData<CommentsResponse>>(
        queryKeys.group.commentsUnread(numericGroupId),
        old => toggleCommentLikeInPages(old, commentId),
      );

      return { previousComments, previousUnreadComments };
    },
    onError: (_error, _commentId, context) => {
      if (context?.previousComments) {
        queryClient.setQueryData(
          queryKeys.group.comments(numericGroupId),
          context.previousComments,
        );
      }
      if (context?.previousUnreadComments) {
        queryClient.setQueryData(
          queryKeys.group.commentsUnread(numericGroupId),
          context.previousUnreadComments,
        );
      }
    },
    onSettled: () => {
      track('give_likes', { item_type: 'comment' });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.comments(numericGroupId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.myMoments(numericGroupId) });
    },
  });
};

const toggleCommentLike = async (groupId: number | string, commentId: number) => {
  const response = await api.post(`/groups/${groupId}/comments/${commentId}/like`);
  return response.data;
};
