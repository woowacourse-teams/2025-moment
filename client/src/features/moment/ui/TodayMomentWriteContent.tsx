import { Card, TextArea } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Send, Star } from 'lucide-react';
import { useState } from 'react';
import * as S from './TodayContent.styles';

interface TodayMomentWriteContent {
  onSubmit: () => void;
}

export const TodayMomentWriteContent = ({ onSubmit }: TodayMomentWriteContent) => {
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
    <S.TodayContentWrapper>
      <Card.TitleContainer
        Icon={Star}
        title="모멘트 공유하기"
        subtitle="뿌듯한 순간, 위로받고 싶은 순간, 기쁜 순간 모든 모멘트를 자유롭게 적어보세요"
      />
      <Card.Content>
        <TextArea
          maxLength={MAX_LENGTH}
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
        <YellowSquareButton
          Icon={Send}
          title="모멘트 공유하기"
          onClick={handleSubmit}
          disabled={text.trim().length === 0}
        />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
