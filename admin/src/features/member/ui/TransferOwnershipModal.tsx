import { ConfirmModal } from "@shared/ui";

interface TransferOwnershipModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  memberNickname: string;
  isLoading: boolean;
}

export function TransferOwnershipModal({
  isOpen,
  onClose,
  onConfirm,
  memberNickname,
  isLoading,
}: TransferOwnershipModalProps) {
  return (
    <ConfirmModal
      isOpen={isOpen}
      onClose={onClose}
      onConfirm={onConfirm}
      title="Transfer Ownership"
      message={`Are you sure you want to transfer group ownership to "${memberNickname}"? This action cannot be undone.`}
      confirmLabel="Transfer"
      isDestructive
      isLoading={isLoading}
    />
  );
}
