import { Modal } from '@/shared/design-system/modal/Modal';
import { ChevronLeft, ChevronRight, Mail, Siren } from 'lucide-react';
import { WriteTime } from '@/shared/ui/writeTime/WriteTime';
import { WriterInfo } from '@/widgets/writerInfo';
import * as S from './MyMomentsCard.styles';
import { theme } from '@/shared/styles/theme';
import { ComplaintModal } from '@/features/complaint/ui/ComplaintModal';
import { MyMomentsItem } from '../types/moments';
import { useMyMomentsCard } from '../hook/useMyMomentsCard';
import { useDeleteMomentMutation } from '../api/useDeleteMomentMutation';
import { useCommentLikeMutation } from '@/features/comment/api/useCommentLikeMutation';
import { useDeleteCommentMutation } from '@/features/comment/api/useDeleteCommentMutation';
import { useCurrentGroup } from '@/features/group/hooks/useCurrentGroup';
import { Trash2, Heart } from 'lucide-react';
import { useMomentLikeMutation } from '../api/useMomentLikeMutation';
import { useImageFallback } from '@/shared/hooks';

const MomentImageWithFallback = ({
  imageUrl,
  onImageClick,
}: {
  imageUrl: string;
  onImageClick: (url: string, e: React.MouseEvent) => void;
}) => {
  const { src, onError } = useImageFallback(imageUrl);
  return (
    <S.MomentImageContainer>
      <S.MomentImage
        src={src}
        onError={onError}
        alt="모멘트 이미지"
        onClick={e => onImageClick(imageUrl, e)}
      />
    </S.MomentImageContainer>
  );
};

const CommentImageWithFallback = ({
  imageUrl,
  onImageClick,
}: {
  imageUrl: string;
  onImageClick: (url: string, e: React.MouseEvent) => void;
}) => {
  const { src, onError } = useImageFallback(imageUrl);
  return (
    <S.CommentImageContainer>
      <S.CommentImage
        src={src}
        onError={onError}
        alt="코멘트 이미지"
        onClick={e => onImageClick(imageUrl, e)}
      />
    </S.CommentImageContainer>
  );
};

export const MyMomentsCard = ({ myMoment }: { myMoment: MyMomentsItem }) => {
  const { currentGroupId } = useCurrentGroup();
  const {
    isOpen,
    isComplaintOpen,
    currentComment,
    fullImageSrc,
    sortedComments,
    navigation,
    handleModalClose,
    handleMomentClick,
    handleComplaintOpen,
    handleComplaintClose,
    handleComplaintSubmit,
    handleImageClick,
    closeFullImage,
    ImageOverlayPortal,
  } = useMyMomentsCard(myMoment, currentGroupId || '');

  const deleteMomentMutation = useDeleteMomentMutation(currentGroupId || '');
  const likeMomentMutation = useMomentLikeMutation(currentGroupId || '');
  const likeCommentMutation = useCommentLikeMutation(currentGroupId || '');
  const deleteCommentMutation = useDeleteCommentMutation(currentGroupId || '');

  const handleDeleteMoment = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (window.confirm('정말로 이 모멘트를 삭제하시겠습니까?')) {
      deleteMomentMutation.mutate(myMoment.momentId || myMoment.id);
    }
  };

  const handleLikeMoment = (e: React.MouseEvent) => {
    e.stopPropagation();
    likeMomentMutation.mutate(myMoment.momentId || myMoment.id);
  };

  const handleLikeComment = (commentId: number) => {
    likeCommentMutation.mutate(commentId);
  };

  const handleDeleteComment = (commentId: number) => {
    if (window.confirm('정말로 이 코멘트를 삭제하시겠습니까?')) {
      deleteCommentMutation.mutate(commentId);
    }
  };

  const hasComments = myMoment.comments ? myMoment.comments.length > 0 : false;

  return (
    <>
      <S.MyMomentsCard
        key={myMoment.momentId || myMoment.id}
        $hasComment={hasComments}
        onClick={hasComments ? handleMomentClick : undefined}
        $shadow={!myMoment.momentNotification.isRead}
        role={hasComments ? 'button' : undefined}
        tabIndex={hasComments ? 0 : undefined}
        onKeyDown={
          hasComments
            ? e => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleMomentClick();
                }
              }
            : undefined
        }
        aria-label={`${myMoment.content}에 달린 코멘트 확인하기`}
      >
        <S.MyMomentsTitleWrapper>
          <S.ActionWrapper>
            <S.CommentCountWrapper aria-label={`좋아요 수: ${myMoment.likeCount}개`}>
              <S.LikeButton onClick={handleLikeMoment} aria-label="모멘트 좋아요">
                <Heart
                  size={16}
                  aria-hidden="true"
                  color={theme.colors['red-500']}
                  fill={myMoment.hasLiked ? theme.colors['red-500'] : 'none'}
                />
              </S.LikeButton>
              <span aria-hidden="true">{myMoment.likeCount}</span>
            </S.CommentCountWrapper>

            <S.CommentCountWrapper aria-label={`코멘트 수: ${sortedComments?.length}개`}>
              <Mail size={16} aria-hidden="true" />
              <span aria-hidden="true">{sortedComments?.length}</span>
            </S.CommentCountWrapper>

            <WriteTime date={myMoment.createdAt} />
            <S.DeleteButton onClick={handleDeleteMoment} aria-label="모멘트 삭제">
              <Trash2 size={24} color={theme.colors['red-500']} />
            </S.DeleteButton>
          </S.ActionWrapper>
        </S.MyMomentsTitleWrapper>
        <S.MyMomentsContent>{myMoment.content}</S.MyMomentsContent>
        <S.MyMomentsBottomWrapper>
          {myMoment.imageUrl ? (
            <MomentImageWithFallback imageUrl={myMoment.imageUrl} onImageClick={handleImageClick} />
          ) : (
            <div />
          )}
        </S.MyMomentsBottomWrapper>
      </S.MyMomentsCard>
      {isOpen && (
        <Modal
          isOpen={true}
          onClose={handleModalClose}
          variant="memoji"
          position="center"
          size="small"
          aria-labelledby="moment-modal-title"
          aria-role="dialog"
          aria-modal="true"
        >
          <Modal.Header showCloseButton={true} />
          <Modal.Content>
            {currentComment && (
              <>
                <S.MyMomentsModalContent key={currentComment.id}>
                  <S.CommentContentWrapper>
                    <S.MyMomentsModalHeader>
                      <S.WriterInfoWrapper>
                        <WriterInfo
                          writer={currentComment.memberNickname || currentComment.nickname}
                        />
                      </S.WriterInfoWrapper>
                      <S.TitleWrapper>
                        <WriteTime date={currentComment.createdAt} />
                        <S.ActionWrapper>
                          <S.IconButton
                            onClick={() => handleLikeComment(currentComment.id)}
                            aria-label="코멘트 좋아요"
                          >
                            <Heart
                              size={28}
                              color={theme.colors['red-500']}
                              fill={currentComment.hasLiked ? theme.colors['red-500'] : 'none'}
                            />
                          </S.IconButton>
                          <S.LikeCount>{currentComment.likeCount || 0}</S.LikeCount>
                          <S.IconButton
                            onClick={() => handleDeleteComment(currentComment.id)}
                            className="danger"
                            aria-label="코멘트 삭제"
                          >
                            <Trash2 size={28} color={theme.colors['red-500']} />
                          </S.IconButton>
                          <S.ComplaintButton
                            onClick={handleComplaintOpen}
                            aria-label="코멘트 신고하기"
                          >
                            <Siren size={28} color={theme.colors['red-500']} />
                          </S.ComplaintButton>
                        </S.ActionWrapper>
                      </S.TitleWrapper>
                    </S.MyMomentsModalHeader>
                    <S.CommentContainer>
                      {navigation.hasPrevious && (
                        <S.CommentNavigationButton
                          onClick={navigation.goToPrevious}
                          position="left"
                          aria-label="이전 코멘트 보기"
                        >
                          <ChevronLeft size={16} />
                        </S.CommentNavigationButton>
                      )}

                      <S.CommentContent>
                        <div area-label={`${currentComment.content}`}>{currentComment.content}</div>
                        {currentComment.imageUrl && (
                          <CommentImageWithFallback
                            imageUrl={currentComment.imageUrl}
                            onImageClick={handleImageClick}
                          />
                        )}
                      </S.CommentContent>

                      {navigation.hasNext && (
                        <S.CommentNavigationButton
                          onClick={navigation.goToNext}
                          position="right"
                          aria-label="다음 코멘트 보기"
                        >
                          <ChevronRight size={16} />
                        </S.CommentNavigationButton>
                      )}
                    </S.CommentContainer>
                    <S.CommentIndicator>
                      {navigation.currentIndex + 1} / {sortedComments?.length || 0}
                    </S.CommentIndicator>
                  </S.CommentContentWrapper>
                </S.MyMomentsModalContent>
              </>
            )}
          </Modal.Content>
        </Modal>
      )}
      {isComplaintOpen && (
        <ComplaintModal
          isOpen={isComplaintOpen}
          onClose={handleComplaintClose}
          targetId={currentComment.id}
          targetType="COMMENT"
          memberId={currentComment.memberId}
          onSubmit={handleComplaintSubmit}
        />
      )}
      {fullImageSrc && (
        <ImageOverlayPortal>
          <S.ImageOverlay onClick={closeFullImage}>
            <S.FullscreenImage src={fullImageSrc} alt="전체 이미지" />
          </S.ImageOverlay>
        </ImageOverlayPortal>
      )}
    </>
  );
};
