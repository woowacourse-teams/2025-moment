import { useSendExtraMoments } from '@/features/moment/hook/useSendExtraMoments';
import { TodayMomentForm } from '@/features/moment/ui/TodayMomentForm';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import * as S from './index.styles';

export default function TodayMomentExtraPage() {
  const {
    handleExtraContentChange,
     handleExtraImageChange
    handleTagNameClick,
    handleSendExtraContent,
    content,
    tagNames,
  } = useSendExtraMoments();

  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 모멘트"
        subtitle="하루에 한 번, 당신의 특별한 모멘트를 공유해보세요"
      />
      <TodayMomentForm
        handleContentChange={handleExtraContentChange}
        handleImageChange={handleExtraImageChange}
        handleTagNameClick={handleTagNameClick}
        handleSendContent={handleSendExtraContent}
        content={content}
        tagNames={tagNames}
      />
    </S.TodayPageWrapper>
  );
}
