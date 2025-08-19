import { MyMomentsList } from '@/features/moment/ui/MyMomentsList';
import { CollectionHeader } from '@/pages/collection/CollectionHeader';
import * as S from '../index.styles';

export default function MyMomentCollectionPage() {
  return (
    <S.CollectionContainer>
      <CollectionHeader />
      <S.Description>내가 공유한 모멘트와 받은 코멘트를 확인해보세요</S.Description>
      <MyMomentsList />
    </S.CollectionContainer>
  );
}
