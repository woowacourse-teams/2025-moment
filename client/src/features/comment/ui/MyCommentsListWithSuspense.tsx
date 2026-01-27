import { MyCommentsCard } from '@/features/comment/ui/MyCommentsCard';
import { useIntersectionObserver } from '@/shared/hooks';
import * as S from './MyCommentsList.styles';
import { CommentItem, CommentsResponse, FilterType } from '../types/comments';
import { useUnreadCommentsSuspenseQuery } from '../api/useUnreadCommentsSuspenseQuery';
import { useCommentsSuspenseQuery } from '../api/useCommentsSuspenseQuery';
import { NotFound } from '@/shared/ui/notFound/NotFound';
import { SuspenseSkeleton } from '@/shared/ui/skeleton';

interface MyCommentsListWithSuspenseProps {
  filterType: FilterType;
  groupId: string | number;
}

/**
 * @example
 * <ErrorBoundary fallback={<ErrorUI />}>
 *   <Suspense fallback={<SuspenseSkeleton variant="comment" />}>
 *     <MyCommentsListWithSuspense filterType="all" groupId={groupId} />
 *   </Suspense>
 * </ErrorBoundary>
 */
export const MyCommentsListWithSuspense = ({
  filterType,
  groupId,
}: MyCommentsListWithSuspenseProps) => {
  const isUnreadFilter = filterType === 'unread';

  // 필터에 따라 필요한 쿼리만 호출
  const allCommentsQuery = useCommentsSuspenseQuery(groupId);
  const unreadCommentsQuery = useUnreadCommentsSuspenseQuery(groupId);

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage } = isUnreadFilter
    ? unreadCommentsQuery
    : allCommentsQuery;

  const currentComments: CommentItem[] =
    data?.pages.flatMap((page: CommentsResponse) => page.data.comments) || [];
  const hasComments = currentComments.length > 0;

  const observerRef = useIntersectionObserver({
    onIntersect: () => {
      if (hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    enabled: hasNextPage && !isFetchingNextPage,
  });

  return (
    <S.MyCommentsListContainer>
      {hasComments ? (
        <>
          {currentComments.map(myComment => (
            <MyCommentsCard key={myComment.id} myComment={myComment} />
          ))}

          <div ref={observerRef} style={{ height: '1px' }} />

          {isFetchingNextPage && <SuspenseSkeleton variant="comment" count={3} />}
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
