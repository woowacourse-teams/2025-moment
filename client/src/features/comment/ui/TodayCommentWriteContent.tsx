import { ROUTES } from '@/app/routes/routes';
import { toast } from '@/shared/store/toast';
import { FileUpload } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Send } from 'lucide-react';
import { useNavigate } from 'react-router';
import * as S from '../../moment/ui/TodayContent.styles';
import { useSendComments } from '../hooks/useSendComments';
import { Card } from '@/shared/design-system/card';
import { TextArea } from '@/shared/design-system/textArea';

export const TodayCommentWriteContent = ({
  isLoggedIn,
  momentId,
  groupId,
}: {
  isLoggedIn: boolean;
  momentId: number;
  groupId?: string | number;
}) => {
  const MAX_LENGTH = 200;

  const { comment, handleChange, handleImageChange, handleSubmit, isPending } = useSendComments({
    groupId: groupId || '',
    momentId,
  });

  const currentLength = comment.length;
  const isDisabled = comment.trim().length === 0 || currentLength > MAX_LENGTH;

  const navigate = useNavigate();

  const handleTextAreaFocus = (e: React.FocusEvent<HTMLTextAreaElement>) => {
    if (!isLoggedIn) {
      e.preventDefault();
      e.target.blur();
      toast.warning('Moment에 오신 걸 환영해요! 로그인하고 시작해보세요 💫');
      navigate(ROUTES.LOGIN);
      return;
    }
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isDisabled && !isPending && isLoggedIn) {
      handleSubmit();
    }
  };

  return (
    <S.TodayContentForm onSubmit={handleFormSubmit}>
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
          leftIcon={<Send size={16} />}
          type="submit"
          disabled={isPending || isDisabled || !isLoggedIn}
        >
          코멘트 보내기
        </YellowSquareButton>
      </Card.Action>
    </S.TodayContentForm>
  );
};
