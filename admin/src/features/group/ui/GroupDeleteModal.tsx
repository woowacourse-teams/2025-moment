import { Modal, Button } from "@shared/ui";
import * as S from "./GroupEditModal.styles";

interface GroupDeleteModalProps {
  isOpen: boolean;
  onClose: () => void;
  reason: string;
  onReasonChange: (reason: string) => void;
  onConfirm: () => void;
  isLoading: boolean;
}

export function GroupDeleteModal({
  isOpen,
  onClose,
  reason,
  onReasonChange,
  onConfirm,
  isLoading,
}: GroupDeleteModalProps) {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Delete Group"
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={onConfirm}
            disabled={!reason.trim() || isLoading}
            isLoading={isLoading}
          >
            Delete
          </Button>
        </>
      }
    >
      <S.WarningText>
        This action will soft-delete the group. All members will lose access.
      </S.WarningText>
      <S.InputGroup>
        <S.Label htmlFor="delete-group-reason">Reason *</S.Label>
        <S.TextArea
          id="delete-group-reason"
          placeholder="Enter the reason for deletion"
          value={reason}
          onChange={(e) => onReasonChange(e.target.value)}
        />
      </S.InputGroup>
    </Modal>
  );
}
