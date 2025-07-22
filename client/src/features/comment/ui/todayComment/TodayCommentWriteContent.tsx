import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Card, TextArea } from '@/shared/ui';
import { Send } from 'lucide-react';
import { useState } from 'react';
import * as S from '@/features/todayMoment/ui/TodayContent.styles';

interface TodayCommentWriteContent {
  onSubmit: () => void;
}

export const TodayCommentWriteContent = ({ onSubmit }: TodayCommentWriteContent) => {
  const [text, setText] = useState('');
  const MAX_LENGTH = 300;
  const NOT_TEXT = text.trim().length === 0;

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
        title={''} //추후 공용 컴포넌트 추가 후 수정
        subtitle="오늘 첫 면접에서 떨어졌어요. 너무 실망스럽고 자신감이 없어져요. 위로 받고 싶어요."
      />
      <Card.Content>
        <TextArea
          placeholder="따뜻한 위로의 말을 전해주세요..."
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
          title="코멘트 보내기"
          onClick={handleSubmit}
          disabled={NOT_TEXT}
        />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
