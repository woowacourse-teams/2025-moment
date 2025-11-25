import { MyMomentsListWithSuspense } from '@/features/moment/ui/MyMomentsListWithSuspense';
import { CollectionHeader } from '@/pages/collection/CollectionHeader';
import { ErrorBoundary, SuspenseSkeleton } from '@/shared/ui';
import { Suspense } from 'react';
import * as S from '../index.styles';

export default function MyMomentCollectionPage() {
  return (
    <S.CollectionContainer>
      <CollectionHeader />
      <S.Description>내가 공유한 모멘트와 받은 코멘트를 확인해보세요</S.Description>

      <ErrorBoundary>
        <Suspense fallback={<SuspenseSkeleton variant="moment" count={3} />}>
          <MyMomentsListWithSuspense />
        </Suspense>
      </ErrorBoundary>
    </S.CollectionContainer>
  );
}
