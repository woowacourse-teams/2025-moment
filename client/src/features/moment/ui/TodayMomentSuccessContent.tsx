import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Card } from '@/shared/ui';
import { CardSuccessContainer } from '@/widgets/today/CardSuccessContainer';
import { CheckCircle, MessageSquare } from 'lucide-react';
import * as S from './TodayContent.styles';
import { useNavigate } from 'react-router';

export const TodayMomentSuccessContent = () => {
  const navigate = useNavigate();

  const handleNavigate = () => navigate('/my-moments');

  return (
    <S.TodayContentWrapper>
      <Card.Content>
        <CardSuccessContainer
          Icon={CheckCircle}
          title="오늘의 모멘트를 공유했어요!"
          subtitle={
            '당신의 모멘트가 누군가에게 전달되었습니다.\n내일 또 다른 모멘트를 공유해보세요'
          }
        />
      </Card.Content>
      <Card.Action position="center">
        <YellowSquareButton
          Icon={MessageSquare}
          title="받은 모멘트 보기"
          onClick={handleNavigate}
        />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
