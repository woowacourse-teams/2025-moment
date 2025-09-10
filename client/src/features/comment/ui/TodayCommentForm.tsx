import { Card, NotFound, SimpleCard } from '@/shared/ui';
import { CommonSkeletonCard } from '@/shared/ui/skeleton';
import { AlertCircle, Loader, RotateCcw } from 'lucide-react';
import * as S from './TodayCommentForm.styles';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';
import { WriteTime } from '@/shared/ui/writeTime';
import { WriterInfo } from '@/widgets/writerInfo';
import { GetCommentableMoments } from '../types/comments';

export function TodayCommentForm({
  momentData,
  isLoading,
  isLoggedIn,
  isLoggedInLoading,
  error,
  refetch,
}: {
  momentData?: GetCommentableMoments;
  isLoading: boolean;
  isLoggedIn?: boolean;
  isLoggedInLoading: boolean;
  error: Error | null;
  refetch: () => void;
}) {
  if (isLoggedInLoading) {
    return <CommonSkeletonCard variant="comment" />;
  }

  if (!isLoggedIn) {
    return (
      <Card width="medium">
        <Card.TitleContainer
          title={
            <S.TitleWrapper>
              <WriterInfo writer={'푸르른 물방울의 테리우스'} level={'ASTEROID_WHITE'} />
              <S.ActionWrapper>
                <WriteTime date="9시간 전" />
              </S.ActionWrapper>
            </S.TitleWrapper>
          }
          subtitle=""
        />
        <SimpleCard height="small" content={'다른 사람의 모멘트는 로그인 후에 확인할 수 있어요!'} />
        <TodayCommentWriteContent isLoggedIn={isLoggedIn ?? false} momentId={0} />
      </Card>
    );
  }

  if (isLoading) {
    return <CommonSkeletonCard variant="comment" />;
  }
  if (!momentData) {
    return (
      <NotFound
        title="누군가 모멘트를 보내길 기다리고 있어요"
        subtitle=""
        icon={Loader}
        size="large"
      />
    );
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
            <WriterInfo writer={momentData.nickname} level={momentData.level} />
            <S.ActionWrapper>
              <WriteTime date={momentData.createdAt} />
              <S.RefreshButton onClick={() => refetch()}>
                <RotateCcw size={28} />
              </S.RefreshButton>
            </S.ActionWrapper>
          </S.TitleWrapper>
        }
        subtitle=""
      />
      <SimpleCard height="small" content={momentData.content} />
      <TodayCommentWriteContent
        momentId={momentData.id}
        isLoggedIn={isLoggedIn}
        key={momentData.id}
      />
    </Card>
  );
}
