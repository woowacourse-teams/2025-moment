import { Modal } from "@shared/ui";
import { Button } from "@shared/ui";
import * as S from "./UserEditModal.styles";

interface UserDeleteModalProps {
  isOpen: boolean;
  onClose: () => void;
  reason: string;
  onReasonChange: (reason: string) => void;
  onConfirm: () => void;
  isLoading: boolean;
}

export function UserDeleteModal({
  isOpen,
  onClose,
  reason,
  onReasonChange,
  onConfirm,
  isLoading,
}: UserDeleteModalProps) {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Delete User"
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
        This action cannot be undone. The user will be soft-deleted.
      </S.WarningText>
      <S.InputGroup>
        <S.Label htmlFor="delete-reason">Reason *</S.Label>
        <S.TextArea
          id="delete-reason"
          placeholder="Enter the reason for deletion"
          value={reason}
          onChange={(e) => onReasonChange(e.target.value)}
        />
      </S.InputGroup>
    </Modal>
  );
}
