import { Card, TextArea } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Send } from 'lucide-react';
import * as S from '../../moment/ui/TodayContent.styles';
import { useSendComments } from '../hooks/useSendComments';

export const TodayCommentWriteContent = () => {
  const MAX_LENGTH = 300;

  const { momentsData, comment, handleChange, handleSubmit } = useSendComments();
  const currentLength = comment.length;
  const isDisabled = comment.trim().length === 0 || currentLength > MAX_LENGTH;

  return (
    <S.TodayContentWrapper>
      <Card.TitleContainer
        title={''} // 추후 공용 컴포넌트 추가 후 수정
        subtitle={momentsData || ''}
      />
      <Card.Content>
        <TextArea
          placeholder="따뜻한 위로의 말을 전해주세요..."
          height="medium"
          maxLength={MAX_LENGTH}
          value={comment}
          onChange={handleChange}
        />
      </Card.Content>
      <Card.Action position="space-between">
        <p>
          {currentLength} / {MAX_LENGTH}
        </p>
        <YellowSquareButton
          Icon={Send}
          title="코멘트 보내기"
          onClick={handleSubmit}
          disabled={isDisabled}
        />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
