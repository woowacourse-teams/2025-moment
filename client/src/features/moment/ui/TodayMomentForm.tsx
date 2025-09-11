import { ROUTES } from '@/app/routes/routes';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useToast } from '@/shared/hooks/useToast';
import { Card, FileUpload, TextArea } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Send, Star } from 'lucide-react';
import { useNavigate } from 'react-router';
import * as S from './TodayContent.styles';
import { checkProfanityWord } from '@/converter/util/checkProfanityWord';
import { TagList } from '@/shared/ui/tag/TagList';
import { TAGS } from '../const/tags';

export function TodayMomentForm({
  handleContentChange,
  handleImageChange,
  handleSendContent,
  handleTagNameClick,
  content,
  tagNames,
}: {
  handleContentChange: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
  handleImageChange: (imageData: { imageUrl: string; imageName: string } | null) => void;
  handleSendContent: () => void;
  handleTagNameClick: (tagName: string) => void;
  content: string;
  tagNames: string[];
}) {
  const navigate = useNavigate();
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  const { showError } = useToast();

  const handleNavigateToTodayMomentSuccess = () => {
    if (checkProfanityWord(content)) {
      showError('모멘트에 부적절한 단어가 포함되어 있습니다.');
    if (tagNames.length === 0) {
      showError('태그를 선택해주세요.');
      return;
    }
    handleSendContent();
    navigate(ROUTES.TODAY_MOMENT_SUCCESS);
  };

  const handleTextAreaFocus = (e: React.FocusEvent<HTMLTextAreaElement>) => {
    if (!isLoggedIn) {
      e.preventDefault();
      e.target.blur();
      showError('로그인 후 이용해주세요');
      navigate(ROUTES.LOGIN);
      return;
    }
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
          <S.TagWrapper>
            <S.TagLabel>태그: </S.TagLabel>
            <TagList tags={TAGS} onTagClick={handleTagNameClick} selectedTag={tagNames} />
          </S.TagWrapper>
          <TextArea
            maxLength={MAX_LENGTH}
            placeholder="오늘 어떤 모멘트를 경험하셨나요? 솔직한 마음을 들려주세요..."
            height="medium"
            value={content}
            onChange={handleContentChange}
            onFocus={handleTextAreaFocus}
          />
          <FileUpload onImageChange={handleImageChange} disabled={false} />
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
