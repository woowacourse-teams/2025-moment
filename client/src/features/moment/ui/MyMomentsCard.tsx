import { CustomTheme, theme } from '@/app/styles/theme';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { Timer } from 'lucide-react';
import * as S from './MyMomentsCard.styles';
import type { MomentWithNotifications } from '../types/momentsWithNotifications';
import { useReadNotifications } from '../../notification/hooks/useReadNotifications';
import { useModal } from '@/shared/hooks/useModal';
import { Modal } from '@/shared/ui/modal/Modal';
import { Heart } from 'lucide-react';
import { Level } from '@/app/layout/ui/Navbar';
import { levelMap } from '@/app/layout/data/navItems';
import { Button } from '@/shared/ui';
import { useEchoSelection } from '@/features/echo/hooks/useEchoSelection';
import { EchoButtonGroup } from '@/features/echo/ui/EchoButtonGroup';
import { useEchoMutation } from '@/features/echo/hooks/useEchoMutation';

export const MyMomentsCard = ({ myMoment }: { myMoment: MomentWithNotifications }) => {
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();
  const { handleOpen, handleClose, isOpen } = useModal();
  const { selectedEchos, toggleEcho, clearSelection, isSelected, hasSelection } =
    useEchoSelection();
  const { mutateAsync: sendEchos } = useEchoMutation();

  const handleModalClose = () => {
    clearSelection();
    handleClose();
  };

  const handleMomentClick = () => {
    handleOpen();
    if (myMoment.read || isReadingNotification) return;
    if (myMoment.notificationId) {
      handleReadNotifications(myMoment.notificationId);
    }
  };

  const handleSendEchos = () => {
    sendEchos({ echoTypes: Array.from(selectedEchos), commentId: myMoment.comments[0].id });
    clearSelection();
  };

  const getFormattedTime = (dateString: string) => {
    try {
      if (!dateString) return '시간 정보 없음';
      return formatRelativeTime(dateString);
    } catch (error) {
      console.error('Date formatting error:', error, 'dateString:', dateString);
      return '시간 정보 오류';
    }
  };

  const hasComments = myMoment.comments.length > 0;

  return (
    <>
      <S.MyMomentsCard
        key={myMoment.id}
        $hasComment={hasComments}
        onClick={hasComments ? handleMomentClick : undefined}
        $shadow={!myMoment.read}
      >
        <S.TitleWrapper>
          <Timer size={16} color={theme.colors['gray-400']} />
          <S.TimeStamp>{getFormattedTime(myMoment.createdAt)}</S.TimeStamp>
        </S.TitleWrapper>
        <S.MyMomentsContent>{myMoment.content}</S.MyMomentsContent>
      </S.MyMomentsCard>

      {isOpen && hasComments && (
        <Modal
          isOpen={true}
          onClose={handleModalClose}
          variant="memoji"
          position="center"
          size="small"
        >
          <Modal.Header showCloseButton={true} />
          <Modal.Content>
            <S.MyMomentsModalContent>
              <S.MyMomentsModalHeader>
                <S.CommenterInfoContainer>
                  <S.LevelIcon
                    src={levelMap[myMoment.comments[0].commenterLevel as Level]}
                    alt="level"
                  />
                  <span>{myMoment.comments[0].commenterName}</span>
                </S.CommenterInfoContainer>
                <S.TitleWrapper>
                  <Timer size={16} color={theme.colors['gray-400']} />
                  <S.TimeStamp>{getFormattedTime(myMoment.comments[0].createdAt)}</S.TimeStamp>
                </S.TitleWrapper>
              </S.MyMomentsModalHeader>

              <S.CommentContainer>
                <div>{myMoment.comments[0].content}</div>
              </S.CommentContainer>

              <S.EchoContainer>
                <S.TitleContainer>
                  <Heart size={20} color={theme.colors['yellow-500']} />
                  <span>에코 보내기</span>
                </S.TitleContainer>
                <S.EchoButtonContainer>
                  <EchoButtonGroup onToggle={toggleEcho} isSelected={isSelected} />
                </S.EchoButtonContainer>
                <Button
                  title="전송"
                  variant="primary"
                  disabled={!hasSelection}
                  externalVariant={() => buttonVariant(theme, hasSelection)}
                  onClick={handleSendEchos}
                />
              </S.EchoContainer>
            </S.MyMomentsModalContent>
          </Modal.Content>
        </Modal>
      )}
    </>
  );
};

const buttonVariant = (theme: CustomTheme, isSelected: boolean) => `
  width: 100%;
  color: ${theme.colors['white']};
  background-color: ${isSelected ? theme.colors['gray-600'] : theme.colors['gray-600_20']};
`;
