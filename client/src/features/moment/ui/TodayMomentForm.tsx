import { Card, TextArea } from '@/shared/ui';
import * as S from './TodayContent.styles';
import { Send, Star } from 'lucide-react';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { useNavigate } from 'react-router';
import { ROUTES } from '@/app/routes/routes';

export function TodayMomentForm({
  handleContentChange,
  handleSendContent,
  content,
}: {
  handleContentChange: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
  handleSendContent: () => void;
  content: string;
}) {
  const navigate = useNavigate();

  const handleNavigateToTodayMomentSuccess = () => {
    handleSendContent();
    navigate(ROUTES.TODAY_MOMENT_SUCCESS);
  };

  const MAX_LENGTH = 200;

  return (
    <Card width="medium">
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
            value={content}
            onChange={handleContentChange}
          />
        </Card.Content>
        <Card.Action position="space-between">
          <p>
            {content.length} / {MAX_LENGTH}
          </p>
          <YellowSquareButton
            Icon={Send}
            title="모멘트 공유하기"
            onClick={handleNavigateToTodayMomentSuccess}
            disabled={content.trim().length === 0}
          />
        </Card.Action>
      </S.TodayContentWrapper>
    </Card>
  );
}
