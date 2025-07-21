import { RequestButton } from '@/features/auth/ui/requestButton';
import { Card, TextArea } from '@/shared/ui';
import { CardSuccessContainer } from '@/shared/ui/card/CardSuccessContainer';
import { CheckCircle, MessageSquare } from 'lucide-react';

interface TodayMomentSuccessContentProps {
  onBack: () => void;
}

export const TodayMomentSuccessContent = ({ onBack }: TodayMomentSuccessContentProps) => {
  return (
    <>
      <Card.Content>
        <CardSuccessContainer
          Icon={CheckCircle}
          title="오늘의 모멘트를 공유했어요!"
          subtitle={
            '당신의 모멘트가 누군가에게 전달되었습니다.\n내일 또 다른 모멘트를 공유해보세요'
          }
        ></CardSuccessContainer>
      </Card.Content>
      <Card.Action position="center">
        <RequestButton Icon={MessageSquare} title="받은 모멘트 보기" onClick={onBack} />
      </Card.Action>
    </>
  );
};
