import { RequestButton } from '@/features/TodayMoment/ui/RequestButton';
import { Card, TextArea } from '@/shared/ui';
import { Send } from 'lucide-react';
import { useState } from 'react';

interface TodayMomentWriteContentProps {
  onSubmit: () => void;
}

export const TodayMomentWriteContent = ({ onSubmit }: TodayMomentWriteContentProps) => {
  const [text, setText] = useState('');
  const MAX_LENGTH = 300;

  const handleTextChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newText = e.target.value;
    if (newText.length <= MAX_LENGTH) {
      setText(newText);
    }
  };

  const handleSubmit = () => {
    onSubmit();
  };
  return (
    <>
      <Card.Content>
        <TextArea
          placeholder="오늘 어떤 모멘트를 경험하셨나요? 솔직한 마음을 들려주세요..."
          height="medium"
          value={text}
          onChange={handleTextChange}
        />
      </Card.Content>
      <Card.Action position="space-between">
        <p>
          {text.length} / {MAX_LENGTH}
        </p>
        <RequestButton
          Icon={Send}
          title="모멘트 공유하기"
          onClick={handleSubmit}
          disabled={text.trim().length === 0}
        />
      </Card.Action>
    </>
  );
};
