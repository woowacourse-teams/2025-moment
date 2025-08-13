import { Card } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { CardSuccessContainer } from '@/widgets/today/CardSuccessContainer';
import { CheckCircle, MessageSquare } from 'lucide-react';
import { useNavigate } from 'react-router';
import * as S from './TodayContent.styles';
import { sendEvent } from '@/shared/lib/ga';

export const TodayMomentSuccessContent = () => {
  const navigate = useNavigate();

  const handleNavigate = () => {
    sendEvent({
      category: 'TodayMoment',
      action: 'Click MoveToTodayComment Button',
      label: 'MoveToTodayComment Button',
    });

    navigate('/today-comment');
  };
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
          title="코멘트 남기러가기"
          onClick={handleNavigate}
        />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
