import { useCommentsQuery } from '@/features/comment/hooks/useCommentsQuery';
import { MyCommentsList } from '@/features/comment/ui/MyCommentsList';
import { CommonSkeletonCard } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import styled from '@emotion/styled';
import { useCallback, useEffect, useRef } from 'react';

export default function MyCommentsPage() {
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

  const observerRef = useRef<HTMLDivElement>(null);

  const handleIntersect = useCallback(
    (entries: globalThis.IntersectionObserverEntry[]) => {
      const [entry] = entries;
      if (entry.isIntersecting && hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    [hasNextPage, isFetchingNextPage, fetchNextPage],
  );

  useEffect(() => {
    if (!observerRef.current) return;

    const observer = new globalThis.IntersectionObserver(handleIntersect, {
      threshold: 0.1,
    });

    observer.observe(observerRef.current);

    return () => observer.disconnect();
  }, [handleIntersect]);

  if (isLoading) {
    return (
      <MyCommentsPageContainer>
        <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={`myComments-skeleton-card-${index}`} variant="comment" />
        ))}
      </MyCommentsPageContainer>
    );
  }

  return (
    <MyCommentsPageContainer>
      <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
      <MyCommentsList myComments={myComments} observerRef={observerRef} />
    </MyCommentsPageContainer>
  );
}

export const MyCommentsPageContainer = styled.section`
  display: flex;
  flex-direction: column;
  gap: 30px;
  margin: 20px;
`;
