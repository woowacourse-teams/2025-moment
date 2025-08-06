import { MyMomentsList } from '@/features/moment/ui/MyMomentsList';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import styled from '@emotion/styled';

export default function MyMoments() {
  return (
    <MyMomentsPageContainer>
      <TitleContainer
        title="나의 모멘트"
        subtitle="내가 공유한 모멘트와 받은 공감을 확인해보세요"
      />
      <MyMomentsList />
    </MyMomentsPageContainer>
  );
}

export const MyMomentsPageContainer = styled.section`
  display: flex;
  flex-direction: column;
  gap: 30px;
  margin: 20px;
`;
