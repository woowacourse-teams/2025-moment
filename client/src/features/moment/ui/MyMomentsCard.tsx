import { theme } from '@/app/styles/theme';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { Timer } from 'lucide-react';
import * as S from './MyMomentsCard.styles';
import type { MomentWithNotifications } from '../types/momentsWithNotifications';
import { useReadNotifications } from '../../notification/hooks/useReadNotifications';
import { useModal } from '@/shared/hooks/useModal';
import { Modal } from '@/shared/ui/modal/Modal';
import { EchoButton } from '@/features/echo/ui/EchoButton';
import { Heart } from 'lucide-react';
import { ECHO_TYPE } from '@/features/echo/const/echoType';
import { Level } from '@/app/layout/ui/Navbar';
import { levelMap } from '@/app/layout/data/navItems';

export const MyMomentsCard = ({ myMoment }: { myMoment: MomentWithNotifications }) => {
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();
  const { handleOpen, handleClose, isOpen } = useModal();

  const handleMomentClick = () => {
    handleOpen();
    if (myMoment.read || isReadingNotification) return;
    if (myMoment.notificationId) {
      handleReadNotifications(myMoment.notificationId);
    }
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

  return (
    <>
      <S.MyMomentsCard
        key={myMoment.id}
        $hasComment={myMoment.comments.length > 0}
        onClick={!!myMoment.comments.length ? handleMomentClick : undefined}
        $shadow={!myMoment.read}
      >
        <S.TitleWrapper>
          <Timer size={16} color={theme.colors['gray-400']} />
          <S.TimeStamp>{getFormattedTime(myMoment.createdAt)}</S.TimeStamp>
        </S.TitleWrapper>
        <S.MyMomentsContent>{myMoment.content}</S.MyMomentsContent>
      </S.MyMomentsCard>
      {isOpen && (
        <Modal isOpen={true} onClose={handleClose} variant="memoji" position="center" size="small">
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
                  <EchoButton title={ECHO_TYPE.THANKS} onClick={handleClose} />
                  <EchoButton title={ECHO_TYPE.TOUCHED} onClick={handleClose} />
                  <EchoButton title={ECHO_TYPE.COMFORTED} onClick={handleClose} />
                </S.EchoButtonContainer>
              </S.EchoContainer>
            </S.MyMomentsModalContent>
          </Modal.Content>
        </Modal>
      )}
    </>
  );
};
