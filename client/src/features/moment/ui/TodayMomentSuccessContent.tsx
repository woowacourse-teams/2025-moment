import { Button, Card } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { CardSuccessContainer } from '@/widgets/today/CardSuccessContainer';
import { CheckCircle, MessageSquare, Plus } from 'lucide-react';
import { useNavigate } from 'react-router';
import * as S from './TodayMomentSuccessContent.styles';
import { useMomentExtraWritableQuery } from '../hook/useMomentExtraWritableQuery';
import { useModal } from '@/shared/hooks/useModal';
import { Modal } from '@/shared/ui/modal/Modal';
import { track } from '@/shared/lib/ga/track';

export const TodayMomentSuccessContent = () => {
  const navigate = useNavigate();
  const { data: momentExtraWritableData } = useMomentExtraWritableQuery();
  const canExtraWritable = momentExtraWritableData?.data?.status === 'ALLOWED';

  const { handleOpen, handleClose, isOpen } = useModal();

  const handleNavigate = () => {
    track('click_navigation', { destination: 'today_comment', source: 'success_page' });
    navigate('/today-comment');
  };

  const handleNavigateToTodayMoment = () => {
    navigate('/today-moment-extra');
  };

  return (
    <S.TodayContentForm>
      <Card.Content>
        <CardSuccessContainer
          Icon={CheckCircle}
          title="오늘의 모멘트를 공유했어요!"
          subtitle={
            '당신의 모멘트가 누군가에게 전달되었습니다.\n내일 또 다른 모멘트를 공유해보세요'
          }
        />
      </Card.Content>
      <Card.Action position="center">
        <S.ActionContainer>
          <YellowSquareButton
            Icon={MessageSquare}
            title="코멘트 남기러가기"
            onClick={handleNavigate}
          />
          <YellowSquareButton Icon={Plus} title="추가 작성하기" onClick={handleOpen} />
        </S.ActionContainer>
      </Card.Action>
      {isOpen && (
        <Modal isOpen={isOpen} onClose={handleClose} size="small">
          <Modal.Header showCloseButton={false} />
          <Modal.Content>
            <S.ModalContent>
              {canExtraWritable ? (
                <p>
                  추가 모멘트를 작성하시겠습니까? <br /> 작성 시 별조각 10개가 차감됩니다.
                </p>
              ) : (
                <p>
                  추가 작성하려면 별조각 10개가 필요합니다. <br /> 별조각을 모아오세요.
                </p>
              )}
            </S.ModalContent>
          </Modal.Content>
          <Modal.Footer>
            <S.ModalActionContainer>
              {canExtraWritable && (
                <Button
                  title="추가 작성하기"
                  variant="secondary"
                  onClick={handleNavigateToTodayMoment}
                />
              )}
              <Button title="닫기" variant="primary" onClick={handleClose} />
            </S.ModalActionContainer>
          </Modal.Footer>
        </Modal>
      )}
    </S.TodayContentForm>
  );
};
