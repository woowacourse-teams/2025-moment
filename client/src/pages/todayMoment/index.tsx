import { TodayMomentForm } from '@/features/moment/ui/TodayMomentForm';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import styled from '@emotion/styled';

export default function TodayMomentPage() {
  return (
    <TodayPageWrapper>
      <TitleContainer
        title="오늘의 모멘트"
        subtitle="하루에 한 번, 당신의 특별한 모멘트를 공유해보세요"
      />
      <TodayMomentForm />
    </TodayPageWrapper>
  );
}

export const TodayPageWrapper = styled.main`
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20px;
`;
