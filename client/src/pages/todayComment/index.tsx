import { useCommentCreationStatusQuery } from '@/features/comment/hooks/useCommentCreationStatusQuery';
import { AlreadyCommentedContent } from '@/features/comment/ui/AlreadyCommentedContent';
import { NotMatchedContent } from '@/features/comment/ui/NotMatchedContent';
import { TodayCommentForm } from '@/features/comment/ui/TodayCommentForm';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import * as S from '../todayMoment/index.styles';

export default function TodayCommentPage() {
  const { data, isLoading, error } = useCommentCreationStatusQuery();

  const renderContent = () => {
    if (isLoading) {
      return <div>로딩 중...</div>;
    }

    if (error || !data) {
      return <div>오류가 발생했습니다.</div>;
    }

    const status = data.data.commentCreationStatus;

    switch (status) {
      case 'NOT_MATCHED':
        return <NotMatchedContent />;
      case 'ALREADY_COMMENTED':
        return <AlreadyCommentedContent />;
      case 'WRITABLE':
        return <TodayCommentForm />;
      default:
        return <TodayCommentForm />;
    }
  };

  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 코멘트"
        subtitle="특별한 모멘트를 공유받고 따뜻한 공감을 보내보세요"
      />
      {renderContent()}
    </S.TodayPageWrapper>
  );
}
