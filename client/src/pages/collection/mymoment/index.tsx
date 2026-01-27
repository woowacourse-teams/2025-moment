import { MyMomentsListWithSuspense } from '@/features/moment/ui/MyMomentsListWithSuspense';
import { CollectionHeader } from '@/pages/collection/CollectionHeader';
import { SuspenseSkeleton } from '@/shared/ui/skeleton';
import { Suspense } from 'react';
import * as S from '../index.styles';
import { NotFound } from '@/shared/ui/notFound/NotFound';
import { ErrorBoundary } from '@/shared/ui/errorBoundary';

import { TodayMomentFilter } from '@/features/moment/ui/TodayMomentFilter';
import { useState } from 'react';
import { FilterType } from '@/features/moment/types/moments';
import { useCurrentGroup } from '@/features/group/hooks/useCurrentGroup';

export default function MyMomentCollectionPage() {
  const { currentGroupId: groupId } = useCurrentGroup();
  const [activeFilter, setActiveFilter] = useState<FilterType>('all');

  const handleActiveFilterChange = (filter: FilterType) => {
    setActiveFilter(filter);
  };

  return (
    <S.CollectionContainer>
      <CollectionHeader />
      <S.Description>내가 공유한 모멘트와 받은 코멘트를 확인해보세요</S.Description>
      <S.FilterWrapper>
        <TodayMomentFilter
          activeFilter={activeFilter}
          onActiveFilterChange={handleActiveFilterChange}
        />
      </S.FilterWrapper>

      <ErrorBoundary
        fallback={() => (
          <NotFound title="모멘트를 불러올 수 없습니다" subtitle="잠시 후 다시 시도해주세요" />
        )}
      >
        <Suspense fallback={<SuspenseSkeleton variant="moment" count={3} />}>
          {groupId && <MyMomentsListWithSuspense filterType={activeFilter} groupId={groupId} />}
        </Suspense>
      </ErrorBoundary>
    </S.CollectionContainer>
  );
}
