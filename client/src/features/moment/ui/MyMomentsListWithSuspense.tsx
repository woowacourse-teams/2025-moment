import { useIntersectionObserver } from '@/shared/hooks';
import { SuspenseSkeleton } from '@/shared/ui/skeleton';
import { Clock } from 'lucide-react';
import { MyMomentsCard } from './MyMomentsCard';
import * as S from './MyMomentsList.styles';
import { useMyMomentsSuspenseQuery } from '../api/useMyMomentsSuspenseQuery';
import { MyMomentsItem, MomentsResponse } from '../types/moments';
import { NotFound } from '@/shared/ui/notFound/NotFound';
import { FilterType } from '../types/moments';
import { useUnreadMomentsSuspenseQuery } from '../api/useUnreadMomentsSuspenseQuery';

interface MyMomentsListWithSuspenseProps {
  filterType?: FilterType;
  groupId: number | string;
}

/**
 * @example
 * <ErrorBoundary fallback={<ErrorUI />}>
 *   <Suspense fallback={<SuspenseSkeleton variant="moment" />}>
 *     <MyMomentsListWithSuspense filterType="all" groupId={groupId} />
 *   </Suspense>
 * </ErrorBoundary>
 */
export const MyMomentsListWithSuspense = ({
  filterType = 'all',
  groupId,
}: MyMomentsListWithSuspenseProps) => {
  const isUnreadFilter = filterType === 'unread';

  const allMomentsQuery = useMyMomentsSuspenseQuery(groupId);
  const unreadMomentsQuery = useUnreadMomentsSuspenseQuery(groupId);

  const {
    data: moments,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = isUnreadFilter ? unreadMomentsQuery : allMomentsQuery;

  const momentItems = moments?.pages.flatMap((page: MomentsResponse) => page.data.moments) || [];
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
            <MyMomentsCard key={myMoment.momentId || myMoment.id} myMoment={myMoment} />
          ))}

          <div ref={observerRef} style={{ height: '1px' }} />

          {isFetchingNextPage && <SuspenseSkeleton variant="moment" count={3} />}
        </>
      ) : (
        <NotFound
          title={isUnreadFilter ? '모든 알림을 확인했습니다' : '아직 모멘트가 없어요'}
          subtitle={isUnreadFilter ? '' : '오늘의 모멘트를 작성하고 따뜻한 공감을 받아보세요'}
          icon={Clock}
          size="large"
        />
      )}
    </S.MomentsContainer>
  );
};
