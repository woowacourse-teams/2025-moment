import { useMemo } from 'react';
import { useEchoSelection } from '@/features/echo/hooks/useEchoSelection';
import { useModal } from '@/shared/hooks/useModal';
import { Modal } from '@/shared/ui/modal/Modal';
import { ChevronLeft, ChevronRight, Mail } from 'lucide-react';
import { useReadNotifications } from '../../notification/hooks/useReadNotifications';
import { useCommentNavigation } from '../hook/useCommentNavigation';
import type { MomentWithNotifications } from '../types/momentsWithNotifications';
import * as S from './MyMomentsCard.styles';
import { WriterInfo } from '@/widgets/writerInfo';
import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { WriteTime } from '@/shared/ui/writeTime';
import { SendEchoForm } from '@/features/echo/ui/SendEchoForm';

export const MyMomentsCard = ({ myMoment }: { myMoment: MomentWithNotifications }) => {
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();
  const { handleOpen, handleClose, isOpen } = useModal();
  useEchoSelection();
  const { data: notifications } = useNotificationsQuery();
  const sortedComments = useMemo(() => {
    return myMoment.comments?.slice().reverse() || [];
  }, [myMoment.comments]);
  const navigation = useCommentNavigation(sortedComments?.length || 0);
  const currentComment = sortedComments?.[navigation.currentIndex];

  const handleModalClose = () => {
    navigation.reset();
    handleClose();
  };

  const handleMomentClick = () => {
    handleOpen();
    navigation.reset();
    if (myMoment.read || isReadingNotification) return;

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
        $shadow={!myMoment.read}
      >
        <S.MyMomentsTitleWrapper>
          <S.CommentCountWrapper>
            <Mail size={16} />
            <span>{sortedComments?.length}</span>
          </S.CommentCountWrapper>
          <WriteTime date={myMoment.createdAt} />
        </S.MyMomentsTitleWrapper>
        <S.MyMomentsContent>{myMoment.content}</S.MyMomentsContent>
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
    </>
  );
};
