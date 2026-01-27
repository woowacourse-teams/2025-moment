import { useState, useEffect } from "react";
import { Modal, Button } from "@shared/ui";
import type { UpdateGroupRequest } from "../types/group";
import * as S from "./GroupEditModal.styles";

interface GroupEditModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentName: string;
  currentDescription: string;
  onSubmit: (data: UpdateGroupRequest) => void;
  isLoading: boolean;
}

const MAX_NAME_LENGTH = 50;
const MAX_DESCRIPTION_LENGTH = 200;

export function GroupEditModal({
  isOpen,
  onClose,
  currentName,
  currentDescription,
  onSubmit,
  isLoading,
}: GroupEditModalProps) {
  const [name, setName] = useState(currentName);
  const [description, setDescription] = useState(currentDescription);

  useEffect(() => {
    setName(currentName);
    setDescription(currentDescription);
  }, [currentName, currentDescription]);

  const isValid =
    name.trim().length > 0 &&
    name.length <= MAX_NAME_LENGTH &&
    description.length <= MAX_DESCRIPTION_LENGTH &&
    (name.trim() !== currentName || description.trim() !== currentDescription);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isValid) return;
    onSubmit({ name: name.trim(), description: description.trim() });
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Edit Group"
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button
            variant="primary"
            type="submit"
            form="edit-group-form"
            disabled={!isValid || isLoading}
            isLoading={isLoading}
          >
            Save
          </Button>
        </>
      }
    >
      <S.Form id="edit-group-form" onSubmit={handleSubmit}>
        <S.InputGroup>
          <S.Label htmlFor="edit-group-name">Name</S.Label>
          <S.Input
            id="edit-group-name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            maxLength={MAX_NAME_LENGTH}
          />
          <S.CharCount>
            {name.length} / {MAX_NAME_LENGTH}
          </S.CharCount>
        </S.InputGroup>
        <S.InputGroup>
          <S.Label htmlFor="edit-group-description">Description</S.Label>
          <S.TextArea
            id="edit-group-description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            maxLength={MAX_DESCRIPTION_LENGTH}
          />
          <S.CharCount>
            {description.length} / {MAX_DESCRIPTION_LENGTH}
          </S.CharCount>
        </S.InputGroup>
      </S.Form>
    </Modal>
  );
}
