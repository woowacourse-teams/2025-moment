import { useIntersectionObserver } from '@/shared/hooks';
import { CommonSkeletonCard, NotFound } from '@/shared/ui';
import { Clock } from 'lucide-react';
import { useMomentsQuery } from '../hook/useMomentsQuery';
import type { MyMomentsItem } from '../types/moments';
import { MyMomentsCard } from './MyMomentsCard';
import * as S from './MyMomentsList.styles';

export const MyMomentsList = () => {
  const {
    data: momentsResponse,
    isLoading,
    isError,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useMomentsQuery();

  if (isError) {
    console.error('Error fetching moments:', error);
    return <div>오류가 발생했습니다. 잠시 후 다시 시도해주세요.</div>;
  }

  const myMoments = momentsResponse?.pages.flatMap(page => page.data.items) ?? [];
  const hasMoments = myMoments?.length && myMoments.length > 0;

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
      <S.MomentsContainer>
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={`moments-skeleton-card-${index}`} variant="moment" />
        ))}
      </S.MomentsContainer>
    );
  }

  return (
    <S.MomentsContainer>
      {hasMoments ? (
        <>
          {myMoments?.map((myMoment: MyMomentsItem, index: number) => (
            <MyMomentsCard key={`${myMoment.createdAt}-${index}`} myMoment={myMoment} />
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
          title="아직 모멘트가 없어요"
          subtitle="오늘의 모멘트를 작성하고 따뜻한 공감을 받아보세요"
          icon={Clock}
          size="large"
        />
      )}
    </S.MomentsContainer>
  );
};
