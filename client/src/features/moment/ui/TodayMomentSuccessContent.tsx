import { ROUTES } from '@/app/routes/routes';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { CardSuccessContainer } from '@/widgets/today/CardSuccessContainer';
import { CheckCircle, MessageSquare } from 'lucide-react';
import { useNavigate, useParams } from 'react-router';
import * as S from './TodayMomentSuccessContent.styles';
import { track } from '@/shared/lib/ga/track';
import { Card } from '@/shared/design-system/card';

export const TodayMomentSuccessContent = () => {
  const navigate = useNavigate();

  const { groupId } = useParams<{ groupId: string }>();

  const handleNavigate = () => {
    track('click_navigation', { destination: 'today_comment', source: 'success_page' });
    if (groupId) {
      navigate(ROUTES.TODAY_COMMENT.replace(':groupId', groupId));
    }
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
        <S.ActionContainer role="group" aria-label="다음 액션 선택">
          <YellowSquareButton
            Icon={MessageSquare}
            title="코멘트 남기러가기"
            onClick={handleNavigate}
          />
        </S.ActionContainer>
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
