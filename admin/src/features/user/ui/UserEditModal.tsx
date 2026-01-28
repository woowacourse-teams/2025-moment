import { Modal } from "@shared/ui";
import { Button } from "@shared/ui";
import { useUserEdit } from "../hooks/useUserEdit";
import * as S from "./UserEditModal.styles";

interface UserEditModalProps {
  isOpen: boolean;
  onClose: () => void;
  userId: string;
  currentNickname: string;
}

export function UserEditModal({
  isOpen,
  onClose,
  userId,
  currentNickname,
}: UserEditModalProps) {
  const {
    nickname,
    setNickname,
    isValid,
    isPending,
    handleSubmit,
    MAX_NICKNAME_LENGTH,
  } = useUserEdit({
    userId,
    initialNickname: currentNickname,
    onSuccess: onClose,
  });

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Edit Nickname"
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button
            variant="primary"
            onClick={() =>
              handleSubmit({ preventDefault: () => {} } as React.FormEvent)
            }
            disabled={!isValid || isPending}
            isLoading={isPending}
          >
            Save
          </Button>
        </>
      }
    >
      <S.Form onSubmit={handleSubmit}>
        <S.InputGroup>
          <S.Label htmlFor="edit-nickname">Nickname</S.Label>
          <S.Input
            id="edit-nickname"
            type="text"
            placeholder="Enter nickname"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            maxLength={MAX_NICKNAME_LENGTH}
          />
          <S.CharCount>
            {nickname.length} / {MAX_NICKNAME_LENGTH}
          </S.CharCount>
        </S.InputGroup>
      </S.Form>
    </Modal>
  );
}
