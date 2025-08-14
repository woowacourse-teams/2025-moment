import { theme } from '@/app/styles/theme';
import { useDeleteEmoji } from '@/features/emoji/hooks/useDeleteEmoji';
import { Button, Card, SimpleCard } from '@/shared/ui';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { Send, Timer } from 'lucide-react';
import * as S from './MyMomentsList.styles';
import type { MomentWithNotifications } from '../types/momentsWithNotifications';
import { useReadNotifications } from '../../notification/hooks/useReadNotifications';
import { OpenCommentButton } from '@/features/moment/ui/OpenCommentButton';
import { useModal } from '@/shared/hooks/useModal';
import { Modal } from '@/shared/ui/modal/Modal';
import { EmpathyButton } from '@/features/emoji/ui/EmpathyButton';
import { Heart } from 'lucide-react';

export const MyMomentsCard = ({ myMoment }: { myMoment: MomentWithNotifications }) => {
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();

  const getFormattedTime = (dateString: string) => {
    try {
      if (!dateString) return '시간 정보 없음';
      return formatRelativeTime(dateString);
    } catch (error) {
      console.error('Date formatting error:', error, 'dateString:', dateString);
      return '시간 정보 오류';
    }
  };

  const { handleOpen, handleClose, isOpen } = useModal();

  return (
    <>
      <Card key={myMoment.id} width="full" shadow={!myMoment.read}>
        <Card.TitleContainer
          title={
            <S.TitleWrapper>
              <Timer size={16} color={theme.colors['gray-400']} />
              <S.TimeStamp>{getFormattedTime(myMoment.createdAt)}</S.TimeStamp>
            </S.TitleWrapper>
          }
          subtitle={''}
        />
        <Card.Content>{myMoment.content}</Card.Content>
        <Card.Action position="center">
          <OpenCommentButton hasComment={!!myMoment.comment?.content} onClick={handleOpen} />
        </Card.Action>
      </Card>
      {isOpen && (
        <Modal isOpen={true} onClose={handleClose} position="center" size="medium">
          <Modal.Header showCloseButton={true} />
          <Modal.Content>
            <p>{myMoment.content}</p>
            <S.TitleContainer>
              <Send size={20} color={theme.colors['yellow-500']} />
              <span>받은 공감</span>
            </S.TitleContainer>
            <SimpleCard height="small" content={<div>{myMoment.comment?.content}</div>} />
            <S.TitleContainer>
              <Heart size={20} color={theme.colors['yellow-500']} />
              <span>공감 보내기</span>
            </S.TitleContainer>
            <S.EmpathyContainer>
              <EmpathyButton title="마음 고마워요" onClick={handleClose} />
              <EmpathyButton title="감동 받았어요" onClick={handleClose} />
              <EmpathyButton title="위로가 됐어요" onClick={handleClose} />
            </S.EmpathyContainer>
          </Modal.Content>
        </Modal>
      )}
    </>
  );
};
