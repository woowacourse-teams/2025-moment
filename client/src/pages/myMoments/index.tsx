import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import * as S from './index.styles';
import { MyMomentsList } from '@/features/moment/ui/MyMomentsList';

export default function MyMoments() {
  return (
    <S.MyMomentsPageContainer>
      <TitleContainer
        title="나의 모멘트"
        subtitle="내가 공유한 모멘트와 받은 공감을 확인해보세요"
      />
      <MyMomentsList />
    </S.MyMomentsPageContainer>
  );
}
