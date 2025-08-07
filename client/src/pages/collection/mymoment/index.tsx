import { ROUTES } from '@/app/routes/routes';
import { MyMomentsList } from '@/features/moment/ui/MyMomentsList';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { Link } from 'react-router';
import * as S from '../index.styles';

export default function MyMomentCollectionPage() {
  return (
    <S.CollectionContainer>
      <S.SelectButtonContainer>
        <Link to={ROUTES.COLLECTION_MYMOMENT} />
        <Link to={ROUTES.COLLECTION_MYCOMMENT} />
      </S.SelectButtonContainer>
      <TitleContainer
        title="나의 모멘트"
        subtitle={'내가 공유한 모멘트와 받은 공감을 확인해보세요'}
      />
      <MyMomentsList />
    </S.CollectionContainer>
  );
}
