import { Card, TextArea } from '@/shared/ui';
import { Send, Star } from 'lucide-react';
import { RequestButton } from './requestButton';

export function TodayForm() {
  return (
    <Card width="medium">
      <Card.TitleContainer
        Icon={Star}
        title="모멘트 공유하기"
        subtitle="뿌듯한 순간, 위로받고 싶은 순간, 기쁜 순간 모든 모멘트를 자유롭게 적어보세요"
      ></Card.TitleContainer>
      <Card.Content>
        <TextArea
          placeholder="오늘 어떤 모멘트를 경험하셨나요? 솔직한 마음을 들려주세요..."
          height="medium"
        />
      </Card.Content>
      <Card.Action position="space-between">
        <p>0 / 100</p>
        <RequestButton />
      </Card.Action>
    </Card>
  );
}
