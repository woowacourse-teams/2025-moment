import { ROUTES } from '@/app/routes/routes';
import { useToast } from '@/shared/hooks/useToast';
import { Card, FileUpload, TextArea } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Send } from 'lucide-react';
import { useNavigate } from 'react-router';
import * as S from '../../moment/ui/TodayContent.styles';
import { useSendComments } from '../hooks/useSendComments';

export const TodayCommentWriteContent = ({
  isLoggedIn,
  momentId,
}: {
  isLoggedIn: boolean;
  momentId: number;
}) => {
  const MAX_LENGTH = 200;
  const { showWarning } = useToast();
  const { comment, handleChange, handleImageChange, handleSubmit, isPending } =
    useSendComments(momentId);

  const currentLength = comment.length;
  const isDisabled = comment.trim().length === 0 || currentLength > MAX_LENGTH;

  const navigate = useNavigate();

  const handleTextAreaFocus = (e: React.FocusEvent<HTMLTextAreaElement>) => {
    if (!isLoggedIn) {
      e.preventDefault();
      e.target.blur();
      showWarning('Moment에 오신 걸 환영해요! 로그인하고 시작해보세요 💫');
      navigate(ROUTES.LOGIN);
      return;
    }
  };

  return (
    <S.TodayContentWrapper>
      <Card.Content>
        <TextArea
          placeholder="따뜻한 위로의 말을 전해주세요..."
          height="medium"
          maxLength={MAX_LENGTH}
          value={comment}
          onChange={handleChange}
          onFocus={handleTextAreaFocus}
          readOnly={!isLoggedIn}
        />
        <FileUpload onImageChange={handleImageChange} disabled={!isLoggedIn} />
      </Card.Content>
      <Card.Action position="space-between">
        <p>
          {currentLength} / {MAX_LENGTH}
        </p>
        <YellowSquareButton
          Icon={Send}
          title="코멘트 보내기"
          onClick={handleSubmit}
          disabled={isPending || isDisabled || !isLoggedIn}
        />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
