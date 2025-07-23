import { TodayCommentForm } from '@/features/comment/ui/TodayCommentForm';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import * as S from '../todayMoment/index.styles';

export default function TodayCommentPage() {
  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 코멘트"
        subtitle="특별한 모멘트를 공유받고 따뜻한 공감을 보내보세요"
      />
      <TodayCommentForm />
    </S.TodayPageWrapper>
  );
}
