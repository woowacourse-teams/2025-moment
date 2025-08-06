import { useCommentCreationStatusQuery } from '@/features/comment/hooks/useCommentCreationStatusQuery';
import { TodayCommentForm } from '@/features/comment/ui/TodayCommentForm';
import { NotFound } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { CheckCircleIcon, Users } from 'lucide-react';
import * as S from '../todayMoment/index.styles';
import { IconBar } from '@/widgets/icons/IconBar';

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
        return (
          <NotFound
            title="아직 매칭되지 않았어요"
            subtitle="오늘의 모멘트를 먼저 작성하고 매칭을 기다려보세요"
            icon={Users}
            size="large"
          />
        );
      case 'ALREADY_COMMENTED':
        return (
          <NotFound
            title="이미 코멘트를 남겼어요"
            subtitle="내일 다시 코멘트를 작성할 수 있어요!"
            icon={CheckCircleIcon}
            size="large"
          />
        );
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
      <IconBar />
    </S.TodayPageWrapper>
  );
}
