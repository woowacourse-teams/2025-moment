import React, { useState } from 'react';
import { Modal } from '@/shared/design-system/modal/Modal';
import { ComplaintReasonSelector } from './ComplaintReasonSelector';
import * as S from './ComplaintModal.styles';
import { ComplaintFormData, ComplaintReason } from '@/features/complaint/types/complaintType';
import { Button } from '@/shared/design-system/button';

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

  const handleSubmit = () => {
    const formData: ComplaintFormData = {
      reason: selectedReason!,
      targetId,
      targetType,
    };

    onSubmit(formData);
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
