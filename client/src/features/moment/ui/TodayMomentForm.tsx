import { ROUTES } from '@/app/routes/routes';
import { checkProfanityWord } from '@/converter/util/checkProfanityWord';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useToast } from '@/shared/hooks/useToast';
import { Card, FileUpload, TextArea } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { TagList } from '@/shared/ui/tag/TagList';
import { Send, Star } from 'lucide-react';
import { useNavigate } from 'react-router';
import { TAGS } from '../const/tags';
import * as S from './TodayContent.styles';

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
  const { showError, showWarning } = useToast();

  const handleNavigateToTodayMomentSuccess = () => {
    if (checkProfanityWord(content)) {
      showError('모멘트에 부적절한 단어가 포함되어 있습니다.');
      return;
    }

    if (tagNames.length === 0) {
      showError('태그를 선택해주세요.');
      return;
    }

    if (tagNames.length > 3) {
      showError('태그는 최대 3개까지 선택할 수 있습니다.');
      return;
    }

    handleSendContent();
    navigate(ROUTES.TODAY_MOMENT_SUCCESS);
  };

  const handleTextAreaFocus = (e: React.FocusEvent<HTMLTextAreaElement>) => {
    if (!isLoggedIn) {
      e.preventDefault();
      e.target.blur();
      showWarning('Moment에 오신 걸 환영해요! 로그인하고 시작해보세요 💫');
      navigate(ROUTES.LOGIN);
      return;
    }
  };

  const MAX_LENGTH = 200;

  return (
    <Card width="medium">
      <S.TodayContentForm>
        <legend className="sr-only">오늘의 모멘트 작성</legend>
        <Card.TitleContainer
          Icon={Star}
          title="모멘트 공유하기"
          subtitle="뿌듯한 순간, 위로받고 싶은 순간, 기쁜 순간 모든 모멘트를 자유롭게 적어보세요"
        />
        <Card.Content>
          <fieldset>
            <legend className="sr-only">태그 선택(필수, 최대 3개)</legend>
            <S.TagWrapper>
              <S.TagLabel>태그: </S.TagLabel>
              <TagList tags={TAGS} onTagClick={handleTagNameClick} selectedTag={tagNames} />
            </S.TagWrapper>
          </fieldset>
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
