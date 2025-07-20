import { RequestButton } from '@/features/auth/ui/requestButton';
import { Card, TextArea } from '@/shared/ui';
import { Send } from 'lucide-react';

interface TodayWriteContentProps {
  onSubmit: () => void;
}

export const TodayWriteContent = ({ onSubmit }: TodayWriteContentProps) => {
  return (
    <>
      <Card.Content>
        <TextArea
          placeholder="오늘 어떤 모멘트를 경험하셨나요? 솔직한 마음을 들려주세요..."
          height="medium"
        />
      </Card.Content>
      <Card.Action position="space-between">
        <p>0 / 100</p>
        <RequestButton Icon={Send} title="모멘트 공유하기" onClick={onSubmit} />
      </Card.Action>
    </>
  );
};
