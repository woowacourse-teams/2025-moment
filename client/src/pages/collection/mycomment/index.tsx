import { MyCommentsList } from '@/features/comment/ui/MyCommentsList';
import { CollectionHeader } from '@/pages/collection/CollectionHeader';
import * as S from '../index.styles';

export default function MyCommentCollectionPage() {
  return (
    <S.CollectionContainer>
      <CollectionHeader />
      <S.Description>내가 작성한 코멘트와 받은 에코를 확인해보세요</S.Description>
      <MyCommentsList />
    </S.CollectionContainer>
  );
}
