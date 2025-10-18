import { useIntersectionObserver } from '@/shared/hooks';
import { CommonSkeletonCard, NotFound } from '@/shared/ui';
import { AlertCircle, Clock } from 'lucide-react';
import { MyMomentsCard } from './MyMomentsCard';
import * as S from './MyMomentsList.styles';
import { useMomentsQuery } from '../hook/useMomentsQuery';
import { MyMomentsItem } from '../types/moments';

export const MyMomentsList = () => {
  const {
    data: moments,
    isLoading,
    isError,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useMomentsQuery();

  const hasMoments =
    moments?.pages.flatMap(page => page.data.items).length &&
    moments?.pages.flatMap(page => page.data.items).length > 0;

  if (isError) {
    console.error('Error fetching moments:', error);
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
      <S.MomentsContainer $display={!!hasMoments || isLoading}>
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={`moments-skeleton-card-${index}`} variant="moment" />
        ))}
      </S.MomentsContainer>
    );
  }

  return (
    <S.MomentsContainer $display={!!hasMoments || isLoading}>
      {hasMoments ? (
        <>
          {moments?.pages
            .flatMap(page => page.data.items)
            .map((myMoment: MyMomentsItem) => (
              <MyMomentsCard key={myMoment.id} myMoment={myMoment} />
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
        <>
          <NotFound
            title="아직 모멘트가 없어요"
            subtitle="오늘의 모멘트를 작성하고 따뜻한 공감을 받아보세요"
            icon={Clock}
            size="large"
          />
        </>
      )}
    </S.MomentsContainer>
  );
};
