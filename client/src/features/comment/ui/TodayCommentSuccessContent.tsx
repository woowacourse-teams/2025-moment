import { Card } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { CardSuccessContainer } from '@/widgets/today/CardSuccessContainer';
import { Heart, MessageSquare } from 'lucide-react';
import * as S from '../../moment/ui/TodayContent.styles';
import { useNavigate } from 'react-router';

export const TodayCommentSuccessContent = () => {
  const navigate = useNavigate();

  const handleNavigate = () => navigate('/collection');

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
        <YellowSquareButton Icon={MessageSquare} title="모음집 보러가기" onClick={handleNavigate} />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
