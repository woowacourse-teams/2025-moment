import { useIntersectionObserver } from '@/shared/hooks';
import { SuspenseSkeleton } from '@/shared/ui/skeleton';
import { Clock } from 'lucide-react';
import { MyMomentsCard } from './MyMomentsCard';
import * as S from './MyMomentsList.styles';
import { useMomentsSuspenseQuery } from '../api/useMomentsSuspenseQuery';
import { MyMomentsItem } from '../types/moments';
import { NotFound } from '@/shared/ui/notFound/NotFound';

/**
 * @example
 * <ErrorBoundary fallback={<ErrorUI />}>
 *   <Suspense fallback={<SuspenseSkeleton variant="moment" />}>
 *     <MyMomentsListWithSuspense />
 *   </Suspense>
 * </ErrorBoundary>
 */
export const MyMomentsListWithSuspense = () => {
  const {
    data: moments,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useMomentsSuspenseQuery();

  const momentItems = moments?.pages.flatMap(page => page.data.items) || [];
  const hasMoments = momentItems.length > 0;

  const observerRef = useIntersectionObserver({
    onIntersect: () => {
      if (hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    enabled: hasNextPage && !isFetchingNextPage,
  });

  return (
    <S.MomentsContainer $display={hasMoments}>
      {hasMoments ? (
        <>
          {momentItems.map((myMoment: MyMomentsItem) => (
            <MyMomentsCard key={myMoment.id} myMoment={myMoment} />
          ))}

          <div ref={observerRef} style={{ height: '1px' }} />

          {isFetchingNextPage && <SuspenseSkeleton variant="moment" count={3} />}
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
