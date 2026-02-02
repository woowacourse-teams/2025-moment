import { useEffect } from 'react';
import { TodayCommentForm } from '@/features/comment/ui/TodayCommentForm';
import { TitleContainer } from '@/shared/design-system/titleContainer/TitleContainer';
import * as S from '../todayMoment/index.styles';
import { useCommentableMomentsQuery } from '@/features/comment/api/useCommentableMomentsQuery';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useDwell } from '@/shared/lib/ga/hooks/useDwell';
import { track } from '@/shared/lib/ga/track';
import { useParams, useLocation } from 'react-router';

export default function TodayCommentPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const location = useLocation();
  const { data: isLoggedIn, isLoading: isLoggedInLoading } = useCheckIfLoggedInQuery();
  const {
    data: momentData,
    isLoading,
    error,
    refetch,
  } = useCommentableMomentsQuery(groupId, { enabled: isLoggedIn === true });

  useDwell('composer');

  useEffect(() => {
    track('open_composer', {
      entry: location.state?.entry ?? 'nav',
      composer: 'comment',
    });
  }, []);

  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 코멘트"
        subtitle="특별한 모멘트를 공유받고 따뜻한 공감을 보내보세요"
      />
      <TodayCommentForm
        momentData={momentData}
        isLoading={isLoading}
        isLoggedIn={isLoggedIn}
        isLoggedInLoading={isLoggedInLoading}
        error={error}
        refetch={refetch}
        groupId={groupId}
      />
    </S.TodayPageWrapper>
  );
}
