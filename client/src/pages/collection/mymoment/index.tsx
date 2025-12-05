import { MyMomentsListWithSuspense } from '@/features/moment/ui/MyMomentsListWithSuspense';
import { CollectionHeader } from '@/pages/collection/CollectionHeader';
import { ErrorBoundary, SuspenseSkeleton, NotFound } from '@/shared/ui';
import { Suspense } from 'react';
import * as S from '../index.styles';

export default function MyMomentCollectionPage() {
  return (
    <S.CollectionContainer>
      <CollectionHeader />
      <S.Description>내가 공유한 모멘트와 받은 코멘트를 확인해보세요</S.Description>

      <ErrorBoundary fallback={() => <NotFound title="모멘트를 불러올 수 없습니다" subtitle="잠시 후 다시 시도해주세요" />}>
        <Suspense fallback={<SuspenseSkeleton variant="moment" count={3} />}>
          <MyMomentsListWithSuspense />
        </Suspense>
      </ErrorBoundary>
    </S.CollectionContainer>
  );
}
