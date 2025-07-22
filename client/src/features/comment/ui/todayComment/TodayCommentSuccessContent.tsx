import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Card } from '@/shared/ui';
import { CardSuccessContainer } from '@/widgets/today/CardSuccessContainer';
import { Heart, MessageSquare } from 'lucide-react';
import * as S from '@/features/todayMoment/ui/TodayContent.styles';

interface TodayCommentSuccessContent {
  onPagination: () => void;
}

export const TodayCommentSuccessContent = ({ onPagination }: TodayCommentSuccessContent) => {
  return (
    <S.TodayContentWrapper>
      <Card.Content>
        <CardSuccessContainer
          Icon={Heart}
          title="누군가의 모멘트에 함께 했어요!"
          subtitle={
            '당신의 따뜻하고 깊은 공감이 전달되었습니다.\n매일 더 많은 누군가의 모멘트와 함께해보세요. '
          }
        />
      </Card.Content>
      <Card.Action position="center">
        <YellowSquareButton Icon={MessageSquare} title="보낸 모멘트 보기" onClick={onPagination} />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
