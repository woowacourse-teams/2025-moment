import { Card, TextArea } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Send } from 'lucide-react';
import * as S from '../../moment/ui/TodayContent.styles';
import { useSend } from '@/features/comment/hooks/useSend';

interface TodayCommentWriteContent {
  onSubmit: () => void;
}

export const TodayCommentWriteContent = ({ onSubmit }: TodayCommentWriteContent) => {
  const { commentsData, handleChange, handleSubmit } = useSend();

  const MAX_LENGTH = 300;
  const currentLength = commentsData.content.length;
  const isDisabled = commentsData.content.trim().length === 0 || currentLength > MAX_LENGTH;

  const handleFormSubmit = async () => {
    await handleSubmit();
    onSubmit();
  };

  const handleTextChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    if (e.target.value.length <= MAX_LENGTH) {
      handleChange(e);
    }
  };

  return (
    <S.TodayContentWrapper>
      <Card.TitleContainer
        title={''} // 추후 공용 컴포넌트 추가 후 수정
        subtitle="오늘 첫 면접에서 떨어졌어요. 너무 실망스럽고 자신감이 없어져요. 위로 받고 싶어요."
      />
      <Card.Content>
        <TextArea
          placeholder="따뜻한 위로의 말을 전해주세요..."
          height="medium"
          value={commentsData.content}
          onChange={handleTextChange}
        />
      </Card.Content>
      <Card.Action position="space-between">
        <p>
          {currentLength} / {MAX_LENGTH}
        </p>
        <YellowSquareButton
          Icon={Send}
          title="코멘트 보내기"
          onClick={handleFormSubmit}
          disabled={isDisabled}
        />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
