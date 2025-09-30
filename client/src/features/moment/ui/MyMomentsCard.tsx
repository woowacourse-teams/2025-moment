import { useEchoSelection } from '@/features/echo/hooks/useEchoSelection';
import { SendEchoForm } from '@/features/echo/ui/SendEchoForm';
import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { useModal } from '@/shared/hooks/useModal';
import { Modal } from '@/shared/ui/modal/Modal';
import { ChevronLeft, ChevronRight, Mail, Siren } from 'lucide-react';
import Tag from '@/shared/ui/tag/Tag';
import { WriteTime } from '@/shared/ui/writeTime';
import { WriterInfo } from '@/widgets/writerInfo';
import { useMemo, useState } from 'react';
import { useReadNotifications } from '../../notification/hooks/useReadNotifications';
import { useCommentNavigation } from '../hook/useCommentNavigation';
import * as S from './MyMomentsCard.styles';
import { theme } from '@/app/styles/theme';
import { ComplaintModal } from '@/features/complaint/ui/ComplaintModal';
import { useSendComplaint } from '@/features/complaint/hooks/useSendComplaint';
import { useShowFullImage } from '@/shared/hooks/useShowFullImage';
import { changeToCloudfrontUrlFromS3 } from '@/shared/utils/changeToCloudfrontUrlFromS3';
import { MyMomentsItem } from '../types/moments';

export const MyMomentsCard = ({ myMoment }: { myMoment: MyMomentsItem }) => {
  const [complainedCommentId, setComplainedCommentId] = useState<Set<number>>(new Set());

  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();
  const { handleOpen, handleClose, isOpen } = useModal();
  const {
    handleOpen: handleComplaintOpen,
    handleClose: handleComplaintClose,
    isOpen: isComplaintOpen,
  } = useModal();
  useEchoSelection();
  const { data: notifications } = useNotificationsQuery();

  const filteredComments = useMemo(() => {
    return myMoment.comments?.filter(comment => !complainedCommentId.has(comment.id)) || [];
  }, [myMoment.comments, complainedCommentId]);

  const { fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } = useShowFullImage();
  const sortedComments = useMemo(() => {
    return filteredComments?.slice().reverse() || [];
  }, [filteredComments]);

  const navigation = useCommentNavigation(sortedComments?.length || 0);
  const currentComment = sortedComments?.[navigation.currentIndex];

  const { handleComplaintSubmit } = useSendComplaint(() => {
    handleComplaintClose();

    if (currentComment) {
      setComplainedCommentId(prev => new Set([...prev, currentComment.id]));
    }

    if (filteredComments.length <= 1) {
      handleModalClose();
    } else if (navigation.currentIndex >= filteredComments.length - 1) {
      navigation.goToPrevious();
    }
  });

  const handleModalClose = () => {
    navigation.reset();
    handleClose();
  };

  const handleMomentClick = () => {
    handleOpen();
    navigation.reset();
    if (myMoment.momentNotification.isRead || isReadingNotification) return;

    const unreadMomentNotifications =
      notifications?.data.filter(
        notification => notification.targetId === myMoment.id && !notification.isRead,
      ) || [];

    unreadMomentNotifications.forEach(notification => {
      if (notification.id) {
        handleReadNotifications(notification.id);
      }
    });
  };

  const hasComments = myMoment.comments ? myMoment.comments.length > 0 : false;

  return (
    <>
      <S.MyMomentsCard
        key={myMoment.id}
        $hasComment={hasComments}
        onClick={hasComments ? handleMomentClick : undefined}
        $shadow={!myMoment.momentNotification.isRead}
      >
        <S.MyMomentsTitleWrapper>
          <S.CommentCountWrapper>
            <Mail size={16} />
            <span>{sortedComments?.length}</span>
          </S.CommentCountWrapper>
          <WriteTime date={myMoment.createdAt} />
        </S.MyMomentsTitleWrapper>
        <S.MyMomentsContent>{myMoment.content}</S.MyMomentsContent>
        <S.MyMomentsBottomWrapper>
          {myMoment.imageUrl ? (
            <S.MomentImageContainer>
              <S.MomentImage
                src={changeToCloudfrontUrlFromS3(myMoment.imageUrl)}
                alt="모멘트 이미지"
                onClick={e => handleImageClick(changeToCloudfrontUrlFromS3(myMoment.imageUrl!), e)}
              />
            </S.MomentImageContainer>
          ) : (
            <div />
          )}
          <S.MyMomentsTagWrapper>
            {myMoment.tagNames.map((tag: string) => (
              <Tag key={tag} tag={tag} />
            ))}
          </S.MyMomentsTagWrapper>
        </S.MyMomentsBottomWrapper>
      </S.MyMomentsCard>
      {isOpen && (
        <Modal
          isOpen={true}
          onClose={handleModalClose}
          variant="memoji"
          position="center"
          size="small"
        >
          <Modal.Header showCloseButton={true} />
          <Modal.Content>
            {currentComment && (
              <>
                <S.MyMomentsModalContent key={currentComment.id}>
                  <S.CommentContentWrapper>
                    <S.MyMomentsModalHeader>
                      <S.WriterInfoWrapper>
                        <WriterInfo writer={currentComment.nickname} level={currentComment.level} />
                      </S.WriterInfoWrapper>
                      <S.TitleWrapper>
                        <WriteTime date={currentComment.createdAt} />
                        <S.ComplaintButton onClick={handleComplaintOpen}>
                          <Siren size={28} color={theme.colors['red-500']} />
                        </S.ComplaintButton>
                      </S.TitleWrapper>
                    </S.MyMomentsModalHeader>
                    <S.CommentContainer>
                      {navigation.hasPrevious && (
                        <S.CommentNavigationButton
                          onClick={navigation.goToPrevious}
                          position="left"
                        >
                          <ChevronLeft size={16} />
                        </S.CommentNavigationButton>
                      )}

                      <S.CommentContent>
                        <div>{currentComment.content}</div>
                        {currentComment.imageUrl && (
                          <S.CommentImageContainer>
                            <S.CommentImage
                              src={changeToCloudfrontUrlFromS3(currentComment.imageUrl)}
                              alt="코멘트 이미지"
                              onClick={e =>
                                handleImageClick(
                                  changeToCloudfrontUrlFromS3(currentComment.imageUrl!),
                                  e,
                                )
                              }
                            />
                          </S.CommentImageContainer>
                        )}
                      </S.CommentContent>

                      {navigation.hasNext && (
                        <S.CommentNavigationButton onClick={navigation.goToNext} position="right">
                          <ChevronRight size={16} />
                        </S.CommentNavigationButton>
                      )}
                    </S.CommentContainer>
                    <S.CommentIndicator>
                      {navigation.currentIndex + 1} / {sortedComments?.length || 0}
                    </S.CommentIndicator>
                  </S.CommentContentWrapper>
                  <SendEchoForm currentComment={currentComment} />
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
