import { MyCommentsListWithSuspense } from '@/features/comment/ui/MyCommentsListWithSuspense';
import { CollectionHeader } from '@/pages/collection/CollectionHeader';
import { SuspenseSkeleton } from '@/widgets/skeleton';
import * as S from '../index.styles';
import { TodayCommentFilter } from '@/features/comment/ui/TodayCommentFilter';
import { useState, Suspense } from 'react';
import { FilterType } from '@/features/comment/types/comments';
import { ErrorBoundary } from '@sentry/react';
import { NotFound } from '@/widgets/notFound/NotFound';

export default function MyCommentCollectionPage() {
  const [activeFilter, setActiveFilter] = useState<FilterType>('all');

  const handleActiveFilterChange = (filter: FilterType) => {
    setActiveFilter(filter);
  };

  return (
    <S.CollectionContainer>
      <CollectionHeader />
      <S.Description>내가 작성한 코멘트와 받은 에코를 확인해보세요</S.Description>
      <S.FilterWrapper>
        <TodayCommentFilter
          activeFilter={activeFilter}
          onActiveFilterChange={handleActiveFilterChange}
        />
      </S.FilterWrapper>

      <ErrorBoundary
        fallback={() => (
          <NotFound title="코멘트를 불러올 수 없습니다" subtitle="잠시 후 다시 시도해주세요" />
        )}
      >
        <Suspense fallback={<SuspenseSkeleton variant="comment" count={3} />}>
          <MyCommentsListWithSuspense filterType={activeFilter} />
        </Suspense>
      </ErrorBoundary>
    </S.CollectionContainer>
  );
}
