import { MyCommentsCard } from '@/features/comment/ui/MyCommentsCard';
import { useIntersectionObserver } from '@/shared/hooks';
import { CommonSkeletonCard, NotFound } from '@/shared/ui';
import { AlertCircle } from 'lucide-react';
import { useCommentsWithNotifications } from '../hooks/useCommentsWithNotifications';
import * as S from './MyCommentsList.styles';
import { useMemo } from 'react';
import { FilterType } from '../types/comments';

interface MyCommentsList {
  filterType: FilterType;
}

export const MyCommentsList = ({ filterType }: MyCommentsList) => {
  const {
    commentsWithNotifications,
    isLoading,
    isError,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useCommentsWithNotifications();

  const filteredComments = useMemo(() => {
    if (!commentsWithNotifications) return [];

    if (filterType === 'unread') {
      return commentsWithNotifications.filter(comment => !comment.read);
    }
    return commentsWithNotifications;
  }, [commentsWithNotifications, filterType]);

  const hasComments = filteredComments?.length && filteredComments.length > 0;

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
          {filteredComments.map(myComment => (
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
