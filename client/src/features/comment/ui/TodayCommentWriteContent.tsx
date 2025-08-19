import { Card, TextArea } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Send } from 'lucide-react';
import * as S from '../../moment/ui/TodayContent.styles';
import { useSendComments } from '../hooks/useSendComments';

export const TodayCommentWriteContent = ({ momentId }: { momentId: number }) => {
  const MAX_LENGTH = 200;

  const { comment, handleChange, handleSubmit } = useSendComments(momentId);
  const currentLength = comment.length;
  const isDisabled = comment.trim().length === 0 || currentLength > MAX_LENGTH;

  return (
    <S.TodayContentWrapper>
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
