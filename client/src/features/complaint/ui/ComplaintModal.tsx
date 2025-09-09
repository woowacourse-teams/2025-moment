import React, { useState } from 'react';
import { Modal } from '@/shared/ui/modal/Modal';
import { Button } from '@/shared/ui';
import { ComplaintReasonSelector } from './ComplaintReasonSelector';

import { useToast } from '@/shared/hooks/useToast';
import * as S from './ComplaintModal.styles';
import { ComplaintFormData, ComplaintReason } from '@/features/complaint/types/complaintType';

interface ComplaintModal {
  isOpen: boolean;
  onClose: () => void;
  targetId: number;
  targetType: 'MOMENT' | 'COMMENT';
  onSubmit: (data: ComplaintFormData) => void;
}

export const ComplaintModal: React.FC<ComplaintModal> = ({
  isOpen,
  onClose,
  targetId,
  targetType,
  onSubmit,
}) => {
  const [selectedReason, setSelectedReason] = useState<ComplaintReason | null>(null);
  const { showSuccess, showError } = useToast();

  const handleSubmit = () => {
    if (!selectedReason) {
      showError('신고 사유를 선택해주세요.');
      return;
    }

    const formData: ComplaintFormData = {
      reason: selectedReason,
      targetId,
      targetType,
    };

    onSubmit(formData);
    showSuccess('신고가 접수되었습니다.');
    handleClose();
  };

  const handleClose = () => {
    setSelectedReason(null);
    onClose();
  };

  return (
    <Modal isOpen={isOpen} position="center" size="medium" onClose={handleClose}>
      <Modal.Header title="신고하기" showCloseButton={true} />
      <Modal.Content>
        <S.ModalContent>
          <ComplaintReasonSelector
            selectedReason={selectedReason}
            onReasonSelect={setSelectedReason}
          />
        </S.ModalContent>
      </Modal.Content>
      <Modal.Footer>
        <S.ButtonContainer>
          <Button variant="primary" title="취소" onClick={handleClose} />
          <Button
            variant="secondary"
            title="신고하기"
            onClick={handleSubmit}
            disabled={!selectedReason}
          />
        </S.ButtonContainer>
      </Modal.Footer>
    </Modal>
  );
};
