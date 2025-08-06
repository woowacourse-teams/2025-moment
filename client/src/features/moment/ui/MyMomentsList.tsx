import { CommonSkeletonCard, NotFound } from '@/shared/ui';
import { Clock } from 'lucide-react';
import { useCallback, useEffect, useRef } from 'react';
import { useMomentsQuery } from '../hook/useMomentsQuery';
import type { MyMomentsItem } from '../types/moments';
import { MyMomentsCard } from './MyMomentsCard';
import * as S from './MyMomentsList.styles';

export const MyMomentsList = () => {
  const { data, isLoading, fetchNextPage, hasNextPage, isFetchingNextPage } = useMomentsQuery();
  const myMoments = data?.pages.flatMap(page => page.data.items) ?? [];

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

  const hasMoments = myMoments?.length && myMoments.length > 0;

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
            <MyMomentsCard
              key={`${myMoment.createdAt}-${index}`}
              myMoment={myMoment}
              index={index}
            />
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
