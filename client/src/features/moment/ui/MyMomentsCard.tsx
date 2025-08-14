import { theme } from '@/app/styles/theme';
import { SimpleCard } from '@/shared/ui';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { Send, Timer } from 'lucide-react';
import * as S from './MyMomentsCard.styles';
import type { MomentWithNotifications } from '../types/momentsWithNotifications';
import { useReadNotifications } from '../../notification/hooks/useReadNotifications';
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
      <S.MyMomentsCard
        key={myMoment.id}
        $hasComment={!!myMoment.comment?.content}
        onClick={!!myMoment.comment?.content ? handleOpen : undefined}
        $shadow={!myMoment.read}
      >
        <S.TitleWrapper>
          <Timer size={16} color={theme.colors['gray-400']} />
          <S.TimeStamp>{getFormattedTime(myMoment.createdAt)}</S.TimeStamp>
        </S.TitleWrapper>
        <S.MyMomentsContent>{myMoment.content}</S.MyMomentsContent>
      </S.MyMomentsCard>
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
