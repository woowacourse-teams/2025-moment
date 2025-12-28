import { TitleContainer } from '@/shared/design-system/titleContainer/TitleContainer';
import * as S from './index.styles';
import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';
import { Card } from '@/shared/design-system/card';

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
