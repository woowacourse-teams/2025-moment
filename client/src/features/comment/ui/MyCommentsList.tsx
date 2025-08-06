import { useCommentsQuery } from '@/features/comment/hooks/useCommentsQuery';
import { MyCommentsCard } from '@/features/comment/ui/MyCommentsCard';
import { useIntersectionObserver } from '@/shared/hooks';
import { CommonSkeletonCard, NotFound } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import * as S from './MyCommentsList.styles';

export const MyCommentsList = () => {
  const {
    data: commentsResponse,
    isLoading,
    isError,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useCommentsQuery();

  if (isError) {
    console.error('Error fetching comments:', error);
    return <div>오류가 발생했습니다. 잠시 후 다시 시도해주세요.</div>;
  }

  const myComments = commentsResponse?.pages.flatMap(page => page.data.items) ?? [];

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
        <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={`myComments-skeleton-card-${index}`} variant="comment" />
        ))}
      </S.MyCommentsPageContainer>
    );
  }

  const hasComments = myComments?.length > 0;

  return (
    <>
      {hasComments ? (
        <S.MyCommentsListContainer>
          {myComments.map(myComment => (
            <MyCommentsCard key={myComment.id} myComment={myComment} />
          ))}

          <div ref={observerRef} style={{ height: '1px' }} />
        </S.MyCommentsListContainer>
      ) : (
        <NotFound
          title="아직 작성한 코멘트가 없어요"
          subtitle="다른 사용자의 모멘트에 따뜻한 공감을 보내보세요"
        />
      )}
    </>
  );
};
