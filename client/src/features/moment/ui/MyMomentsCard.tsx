import { theme } from '@/app/styles/theme';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { Timer, ChevronLeft, ChevronRight, Heart } from 'lucide-react';
import * as S from './MyMomentsCard.styles';
import type { MomentWithNotifications } from '../types/momentsWithNotifications';
import { useReadNotifications } from '../../notification/hooks/useReadNotifications';
import { useModal } from '@/shared/hooks/useModal';
import { Modal } from '@/shared/ui/modal/Modal';
import { Level } from '@/app/layout/ui/Navbar';
import { levelMap } from '@/app/layout/data/navItems';
import { useEchoSelection } from '@/features/echo/hooks/useEchoSelection';
import { SendEchoButton } from '@/features/echo/ui/SendEchoButton';
import { EchoButton } from '@/features/echo/ui/EchoButton';
import { EchoTypeKey } from '@/features/echo/type/echos';
import { ECHO_TYPE } from '@/features/echo/const/echoType';
import { useState } from 'react';

export const MyMomentsCard = ({ myMoment }: { myMoment: MomentWithNotifications }) => {
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();
  const { handleOpen, handleClose, isOpen } = useModal();
  const { selectedEchos, toggleEcho, clearSelection, isSelected, hasSelection } =
    useEchoSelection();
  const comments = myMoment.comments;

  const [currentCommentIndex, setCurrentCommentIndex] = useState(0);

  const handleModalClose = () => {
    clearSelection();
    setCurrentCommentIndex(0);
    handleClose();
  };

  const handleMomentClick = () => {
    handleOpen();
    setCurrentCommentIndex(0);
    if (myMoment.read || isReadingNotification) return;
    if (myMoment.notificationId) {
      handleReadNotifications(myMoment.notificationId);
    }
  };

  const handlePrevComment = () => {
    setCurrentCommentIndex(prev => Math.max(0, prev - 1));
  };

  const handleNextComment = () => {
    setCurrentCommentIndex(prev => Math.min((comments?.length || 1) - 1, prev + 1));
  };

  const hasComments = myMoment.comments ? myMoment.comments.length > 0 : false;
  const currentComment = comments?.[currentCommentIndex];
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
                    <S.CommenterInfoContainer>
                      <S.LevelIcon
                        src={levelMap[currentComment.commenterLevel as Level]}
                        alt="level"
                      />
                      <span>{currentComment.commenterName}</span>
                    </S.CommenterInfoContainer>
                    <S.CommentIndicator>
                      {currentCommentIndex + 1} / {comments?.length || 0}
                    </S.CommentIndicator>
                    <S.TitleWrapper>
                      <Timer size={16} color={theme.colors['gray-400']} />
                      <S.TimeStamp>
                        {formatRelativeTime(currentComment.createdAt || '')}
                      </S.TimeStamp>
                    </S.TitleWrapper>
                  </S.MyMomentsModalHeader>

                  <S.CommentContainer>
                    {currentCommentIndex > 0 && (
                      <S.CommentNavigationButton onClick={handlePrevComment} position="left">
                        <ChevronLeft size={16} />
                      </S.CommentNavigationButton>
                    )}

                    <S.CommentContent>
                      <div>{currentComment.content}</div>
                    </S.CommentContent>

                    {currentCommentIndex < (comments?.length || 0) - 1 && (
                      <S.CommentNavigationButton onClick={handleNextComment} position="right">
                        <ChevronRight size={16} />
                      </S.CommentNavigationButton>
                    )}
                  </S.CommentContainer>

                  <S.EchoContainer>
                    <S.TitleContainer>
                      <Heart size={16} color={theme.colors['yellow-500']} />
                      <span>에코 보내기</span>
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
                    <SendEchoButton
                      commentId={currentComment.id || 0}
                      selectedEchos={selectedEchos}
                      hasSelection={hasSelection}
                      isDisabled={hasAnyEcho}
                    />
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
