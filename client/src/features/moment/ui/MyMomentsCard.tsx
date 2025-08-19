import { theme } from '@/app/styles/theme';
import { ECHO_TYPE } from '@/features/echo/const/echoType';
import { useEchoSelection } from '@/features/echo/hooks/useEchoSelection';
import { EchoTypeKey } from '@/features/echo/type/echos';
import { EchoButton } from '@/features/echo/ui/EchoButton';
import { SendEchoButton } from '@/features/echo/ui/SendEchoButton';
import { useModal } from '@/shared/hooks/useModal';
import { Modal } from '@/shared/ui/modal/Modal';
import { ChevronLeft, ChevronRight, Heart, Mail } from 'lucide-react';
import { useReadNotifications } from '../../notification/hooks/useReadNotifications';
import { useCommentNavigation } from '../hook/useCommentNavigation';
import type { MomentWithNotifications } from '../types/momentsWithNotifications';
import * as S from './MyMomentsCard.styles';
import { WriterInfo } from '@/widgets/writerInfo';
import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { WriteTime } from '@/shared/ui/writeTime';

export const MyMomentsCard = ({ myMoment }: { myMoment: MomentWithNotifications }) => {
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();
  const { handleOpen, handleClose, isOpen } = useModal();
  const { selectedEchos, toggleEcho, clearSelection, isSelected, hasSelection } =
    useEchoSelection();
  const { data: notifications } = useNotificationsQuery();
  const comments = myMoment.comments;
  const navigation = useCommentNavigation(comments?.length || 0);
  const handleModalClose = () => {
    clearSelection();
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
  const currentComment = comments?.[navigation.currentIndex];
  const echoType = currentComment?.echos.map(echo => echo.echoType);
  const hasAnyEcho = echoType && echoType.length > 0;

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
            <span>{comments?.length}</span>
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
                  <S.MyMomentsModalHeader>
                    <S.WriterInfoWrapper>
                      <WriterInfo writer={currentComment.nickname} level={currentComment.level} />
                    </S.WriterInfoWrapper>

                    <S.CommentIndicator>
                      {navigation.currentIndex + 1} / {comments?.length || 0}
                    </S.CommentIndicator>
                    <S.TitleWrapper>
                      <WriteTime date={currentComment.createdAt} />
                    </S.TitleWrapper>
                  </S.MyMomentsModalHeader>

                  <S.CommentContainer>
                    {navigation.hasPrevious && (
                      <S.CommentNavigationButton onClick={navigation.goToPrevious} position="left">
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

                  <S.EchoContainer>
                    <S.TitleContainer>
                      <Heart size={16} color={theme.colors['yellow-500']} />
                      <span>{hasAnyEcho ? '보낸 에코' : '에코 보내기'}</span>
                    </S.TitleContainer>
                    <S.EchoButtonContainer>
                      {Object.entries(ECHO_TYPE).map(([key, title]) => {
                        const isAlreadySent = currentComment.echos
                          .map(echo => echo.echoType)
                          .includes(key as EchoTypeKey);
                        return (
                          <EchoButton
                            key={key}
                            onClick={() => toggleEcho(key as EchoTypeKey)}
                            title={title}
                            isSelected={isSelected(key as EchoTypeKey)}
                            isAlreadySent={isAlreadySent}
                            isDisabled={hasAnyEcho}
                          />
                        );
                      })}
                    </S.EchoButtonContainer>
                    {!hasAnyEcho && (
                      <SendEchoButton
                        commentId={currentComment.id || 0}
                        selectedEchos={selectedEchos}
                        hasSelection={hasSelection}
                        isDisabled={hasAnyEcho}
                      />
                    )}
                  </S.EchoContainer>
                </S.MyMomentsModalContent>
              </>
            )}
          </Modal.Content>
        </Modal>
      )}
    </>
  );
};
