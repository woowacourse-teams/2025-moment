import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { Link } from 'react-router';

import { ROUTES } from '@/app/routes/routes';
import { MyCommentsList } from '@/features/comment/ui/MyCommentsList';
import * as S from '../index.styles';

export default function MyCommentCollectionPage() {
  return (
    <S.CollectionContainer>
      <S.SelectButtonContainer>
        <Link to={ROUTES.COLLECTION_MYMOMENT} />
        <Link to={ROUTES.COLLECTION_MYCOMMENT} />
      </S.SelectButtonContainer>
      <TitleContainer
        title="나의 코멘트"
        subtitle={'내가 작성한 코멘트와 받은 공감을 확인해보세요'}
      />
      <MyCommentsList />
    </S.CollectionContainer>
  );
}
