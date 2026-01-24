import { Star, Send } from 'lucide-react';
import * as S from './TodayContent.styles';
import { Card } from '@/shared/design-system/card';
import { FileUpload } from '@/shared/ui';
import { TextArea } from '@/shared/design-system/textArea';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { useTodayMomentForm } from '../hook/useTodayMomentForm';

export function TodayMomentForm({
  handleContentChange,
  handleImageChange,
  handleSendContent,
  content,
}: {
  handleContentChange: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
  handleImageChange: (imageData: { imageUrl: string; imageName: string } | null) => void;
  handleSendContent: () => void;
  content: string;
}) {
  const { handleFormSubmit, handleNavigateToTodayMomentSuccess, handleTextAreaFocus, MAX_LENGTH } =
    useTodayMomentForm({ content, handleSendContent });

  return (
    <Card width="medium">
      <S.TodayContentForm onSubmit={handleFormSubmit}>
        <legend className="sr-only">오늘의 모멘트 작성</legend>
        <Card.TitleContainer
          Icon={Star}
          title="모멘트 공유하기"
          subtitle="뿌듯한 순간, 위로받고 싶은 순간, 기쁜 순간 모든 모멘트를 자유롭게 적어보세요"
        />
        <Card.Content>
          <fieldset>
            <legend className="sr-only">모멘트 내용 작성</legend>
            <TextArea
              maxLength={MAX_LENGTH}
              placeholder="오늘 어떤 모멘트를 경험하셨나요? 솔직한 마음을 들려주세요..."
              height="medium"
              value={content}
              onChange={handleContentChange}
              onFocus={handleTextAreaFocus}
            />
            <FileUpload onImageChange={handleImageChange} disabled={false} />
          </fieldset>
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
      </S.TodayContentForm>
    </Card>
  );
}
