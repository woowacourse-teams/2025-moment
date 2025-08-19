import { LEVEL_MAP } from '@/app/layout/data/navItems';
import { useCommentableMomentsQuery } from '@/features/comment/api/useCommentableMomentsQuery';
import { Card, NotFound, SimpleCard } from '@/shared/ui';
import { CommonSkeletonCard } from '@/shared/ui/skeleton';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { AlertCircle, Clock, RotateCcw } from 'lucide-react';
import * as S from './TodayCommentForm.styles';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';
import { useCheckIfLoggedInQuery } from '@/features/auth/hooks/useCheckIfLoggedInQuery';

export function TodayCommentForm() {
  const { data: isLoggedIn, isLoading: isLoggedInLoading } = useCheckIfLoggedInQuery();
  const {
    data: momentData,
    isLoading,
    error,
    refetch,
  } = useCommentableMomentsQuery({ enabled: isLoggedIn === true });

  if (isLoggedInLoading) {
    return <CommonSkeletonCard variant="comment" />;
  }

  if (!isLoggedIn) {
    return (
      <Card width="medium">
        <Card.TitleContainer
          title={
            <S.TitleWrapper>
              <S.UserInfoWrapper>
                <S.NotLoggedIcon src={'/images/firstAsteroid.png'} alt={''} />
                <S.NotLoggedNickname>푸르른 물방울의 테리우스</S.NotLoggedNickname>
              </S.UserInfoWrapper>
              <S.ActionWrapper>
                <S.TimeWrapper>
                  <Clock size={16} />
                  9시간 전
                </S.TimeWrapper>
              </S.ActionWrapper>
            </S.TitleWrapper>
          }
          subtitle=""
        />
        <SimpleCard
          height="small"
          content={
            '오늘은 아침부터 굉장히 바쁜 하루였어요. 매일매일 이런 날로 채우면 언젠가는 취업할 수 있겠죠?'
          }
        />
        <TodayCommentWriteContent isLoggedIn={isLoggedIn ?? false} />
      </Card>
    );
  }

  if (isLoading) {
    return <CommonSkeletonCard variant="comment" />;
  }

  if (error || !momentData) {
    return (
      <NotFound
        title="데이터를 불러올 수 없습니다"
        subtitle="잠시 후 다시 시도해주세요"
        icon={AlertCircle}
        size="large"
      />
    );
  }

  return (
    <Card width="medium">
      <Card.TitleContainer
        title={
          <S.TitleWrapper>
            <S.UserInfoWrapper>
              <S.LevelImage
                src={LEVEL_MAP[momentData.level as keyof typeof LEVEL_MAP]}
                alt={momentData.level}
              />
              <span>{momentData.nickname}</span>
            </S.UserInfoWrapper>
            <S.ActionWrapper>
              <S.TimeWrapper>
                <Clock size={16} />
                {formatRelativeTime(momentData.createdAt)}
              </S.TimeWrapper>
              <S.RefreshButton onClick={() => refetch()}>
                <RotateCcw size={28} />
              </S.RefreshButton>
            </S.ActionWrapper>
          </S.TitleWrapper>
        }
        subtitle=""
      />
      <SimpleCard height="small" content={momentData.content} />
      <TodayCommentWriteContent isLoggedIn={isLoggedIn} />
    </Card>
  );
}
