import { TodayForm } from '@/features/auth/ui/TodayForm';
import * as S from './index.styles';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';

export default function TodayPage() {
  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 모멘트"
        subtitle="하루에 한 번, 당신의 특별한 모멘트를 공유해보세요"
      />
      <TodayForm />
    </S.TodayPageWrapper>
  );
}
