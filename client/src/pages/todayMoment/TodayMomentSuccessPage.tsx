import { Card } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import * as S from './index.styles';
import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';

export default function TodayMomentSuccessPage() {
  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 모멘트"
        subtitle="하루에 한 번, 당신의 특별한 모멘트를 공유해보세요"
      />
      <Card width="medium">
        <TodayMomentSuccessContent />
      </Card>
    </S.TodayPageWrapper>
  );
}
