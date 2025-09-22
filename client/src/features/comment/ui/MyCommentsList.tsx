import { MyCommentsCard } from '@/features/comment/ui/MyCommentsCard';
import { useIntersectionObserver } from '@/shared/hooks';
import { CommonSkeletonCard, NotFound } from '@/shared/ui';
import { AlertCircle } from 'lucide-react';
import { useCommentsWithNotifications } from '../hooks/useCommentsWithNotifications';
import * as S from './MyCommentsList.styles';
import { useMemo } from 'react';
import { FilterType } from '../types/comments';
import { useUnreadCommentsQuery } from '../api/useUnreadCommentsQuery';
import { CommentWithNotifications } from '../types/commentsWithNotifications';

interface MyCommentsList {
  filterType: FilterType;
}

export const MyCommentsList = ({ filterType }: MyCommentsList) => {
  const allCommentsQuery = useCommentsWithNotifications();
  const unreadCommentsQuery = useUnreadCommentsQuery();

  const isUnreadFilter = filterType === 'unread';

  const {
    commentsWithNotifications: allComments,
    isLoading: isLoadingAll,
    isError: isErrorAll,
    error: errorAll,
    fetchNextPage: fetchNextPageAll,
    hasNextPage: hasNextPageAll,
    isFetchingNextPage: isFetchingNextPageAll,
  } = allCommentsQuery;

  const {
    data: unreadCommentsData,
    isLoading: isLoadingUnread,
    isError: isErrorUnread,
    error: errorUnread,
    fetchNextPage: fetchNextPageUnread,
    hasNextPage: hasNextPageUnread,
    isFetchingNextPage: isFetchingNextPageUnread,
  } = unreadCommentsQuery;

  const unreadComments = useMemo(() => {
    if (!unreadCommentsData) return [];

    return unreadCommentsData.pages.flatMap(page =>
      page.data.items.map(item => {
        const relatedNotification = allCommentsQuery.commentsWithNotifications.find(
          commentWithNotification => commentWithNotification.id === item.id,
        );

        return {
          ...item,
          notificationId: relatedNotification?.notificationId || null,
          read: relatedNotification?.read ?? false,
        };
      }),
    );
  }, [unreadCommentsData, allCommentsQuery.commentsWithNotifications]);

  const currentComments: CommentWithNotifications[] = isUnreadFilter ? unreadComments : allComments;
  const isLoading = isUnreadFilter ? isLoadingUnread : isLoadingAll;
  const isError = isUnreadFilter ? isErrorUnread : isErrorAll;
  const error = isUnreadFilter ? errorUnread : errorAll;
  const fetchNextPage = isUnreadFilter ? fetchNextPageUnread : fetchNextPageAll;
  const hasNextPage = isUnreadFilter ? hasNextPageUnread : hasNextPageAll;
  const isFetchingNextPage = isUnreadFilter ? isFetchingNextPageUnread : isFetchingNextPageAll;

  const sortedComments = useMemo(() => {
    return currentComments || [];
  }, [currentComments]);

  const hasComments = sortedComments?.length && sortedComments.length > 0;

  if (isError) {
    console.error('Error fetching comments:', error);
    return (
      <NotFound
        title="데이터를 불러올 수 없습니다"
        subtitle="잠시 후 다시 시도해주세요"
        icon={AlertCircle}
        size="large"
      />
    );
  }

  const observerRef = useIntersectionObserver({
    onIntersect: () => {
      if (hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    enabled: hasNextPage && !isFetchingNextPage,
  });

  if (isLoading) {
    return (
      <S.MyCommentsPageContainer>
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={`myComments-skeleton-card-${index}`} variant="comment" />
        ))}
      </S.MyCommentsPageContainer>
    );
  }

  return (
    <S.MyCommentsListContainer>
      {hasComments ? (
        <>
          {sortedComments.map(myComment => (
            <MyCommentsCard key={myComment.id} myComment={myComment} />
          ))}

          <div ref={observerRef} style={{ height: '1px' }} />

          {isFetchingNextPage && (
            <>
              {Array.from({ length: 3 }).map((_, index) => (
                <CommonSkeletonCard key={`mymoments-loading-skeleton-${index}`} variant="moment" />
              ))}
            </>
          )}
        </>
      ) : (
        <NotFound
          title={
            filterType === 'unread' ? '모든 알림을 확인했습니다' : '아직 작성한 코멘트가 없어요'
          }
          subtitle={
            filterType === 'unread' ? '' : '다른 사용자의 모멘트에 따뜻한 공감을 보내보세요'
          }
        />
      )}
    </S.MyCommentsListContainer>
  );
};
