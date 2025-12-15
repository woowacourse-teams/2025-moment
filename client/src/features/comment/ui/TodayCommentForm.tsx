import { Card } from '@/shared/design-system/card/Card';
import { SimpleCard } from '@/shared/design-system/simpleCard/SimpleCard';
import { CommonSkeletonCard } from '@/shared/ui/skeleton';
import { AlertCircle, Loader, RotateCcw, Siren } from 'lucide-react';
import * as S from './TodayCommentForm.styles';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';
import { WriteTime } from '@/shared/ui/writeTime/WriteTime';
import { WriterInfo } from '@/widgets/writerInfo';
import { theme } from '@/shared/styles/theme';
import { ComplaintModal } from '@/features/complaint/ui/ComplaintModal';
import { useModal } from '@/shared/hooks/useModal';
import { useSendComplaint } from '@/features/complaint/hooks/useSendComplaint';
import { GetCommentableMoments } from '../types/comments';
import { useShowFullImage } from '@/shared/hooks/useShowFullImage';
import { NotFound } from '@/shared/ui/notFound/NotFound';

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
  const { fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } = useShowFullImage();

  const {
    handleOpen: handleComplaintOpen,
    handleClose: handleComplaintClose,
    isOpen: isComplaintOpen,
  } = useModal();

  const { handleComplaintSubmit } = useSendComplaint(handleComplaintClose);

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
    <S.CommentSection aria-label="코멘트 작성">
      <Card width="medium">
        <Card.TitleContainer
          title={
            <S.TitleWrapper>
              <WriterInfo writer={momentData.nickname} level={momentData.level} />
              <S.ActionWrapper>
                <WriteTime date={momentData.createdAt} />
                <S.ComplaintButton onClick={handleComplaintOpen} aria-label="모멘트 신고">
                  <Siren size={28} color={theme.colors['red-500']} />
                </S.ComplaintButton>
                <S.RefreshButton onClick={() => refetch()} aria-label="다른 모멘트 보기">
                  <RotateCcw size={28} />
                </S.RefreshButton>
              </S.ActionWrapper>
            </S.TitleWrapper>
          }
          subtitle=""
        />
        <SimpleCard
          height="small"
          content={
            <S.MyCommentsContentWrapper>
              <S.MomentContent aria-label={`모멘트 내용: ${momentData.content}`}>
                {momentData.content}
              </S.MomentContent>
              {momentData.imageUrl && (
                <S.MomentImageContainer>
                  <S.MomentImage
                    src={momentData.imageUrl}
                    alt="모멘트 이미지"
                    onClick={e => handleImageClick(momentData.imageUrl!, e)}
                  />
                </S.MomentImageContainer>
              )}
            </S.MyCommentsContentWrapper>
          }
        />

        <TodayCommentWriteContent
          momentId={momentData.id}
          isLoggedIn={isLoggedIn}
          key={momentData.id}
        />
      </Card>
      {isComplaintOpen && (
        <ComplaintModal
          isOpen={isComplaintOpen}
          onClose={handleComplaintClose}
          targetId={momentData.id}
          targetType="MOMENT"
          onSubmit={handleComplaintSubmit}
        />
      )}
      {fullImageSrc && (
        <ImageOverlayPortal>
          <S.ImageOverlay onClick={closeFullImage}>
            <S.FullscreenImage src={fullImageSrc} alt="모멘트 이미지 확대" />
          </S.ImageOverlay>
        </ImageOverlayPortal>
      )}
    </S.CommentSection>
  );
}
