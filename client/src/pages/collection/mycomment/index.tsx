import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';

import { MyCommentsList } from '@/features/comment/ui/MyCommentsList';
import { CollectionHeader } from '@/pages/collection/CollectionHeader';
import * as S from '../index.styles';

export default function MyCommentCollectionPage() {
  return (
    <S.CollectionContainer>
      <CollectionHeader />
      <TitleContainer
        title="나의 코멘트"
        subtitle={'내가 작성한 코멘트와 받은 공감을 확인해보세요'}
      />
      <MyCommentsList />
    </S.CollectionContainer>
  );
}
