import { Card } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import * as S from './index.styles';
import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';

export default function TodayMomentSuccessPage() {
  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 모멘트"
        subtitle="특별한 모멘트를 공유받고 따뜻한 공감을 보내보세요"
      />
      <Card width="medium">
        <TodayMomentSuccessContent />
      </Card>
    </S.TodayPageWrapper>
  );
}
