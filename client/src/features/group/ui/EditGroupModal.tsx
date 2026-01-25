import { useEffect, useState } from 'react';
import { useUpdateGroupMutation } from '../api/useUpdateGroupMutation';
import { Group } from '../types/group';
import { Button } from '@/shared/design-system/button/Button';
import { Input } from '@/shared/design-system/input/Input';
import { Modal } from '@/shared/design-system/modal/Modal';
import * as S from './GroupCreateForm.styles'; // Reuse styles

interface EditGroupModalProps {
  group: Group;
  isOpen: boolean;
  onClose: () => void;
}

const MAX_NAME_LENGTH = 50;
const MAX_DESCRIPTION_LENGTH = 200;

export const EditGroupModal = ({ group, isOpen, onClose }: EditGroupModalProps) => {
  const [name, setName] = useState(group.name);
  const [description, setDescription] = useState(group.description || '');
  const updateGroupMutation = useUpdateGroupMutation(group.groupId);

  useEffect(() => {
    if (isOpen) {
      setName(group.name);
      setDescription(group.description || '');
    }
  }, [isOpen, group]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !description.trim()) return;

    try {
      updateGroupMutation.mutate(
        {
          name: name.trim(),
          description: description.trim(),
        },
        {
          onSuccess: () => {
            onClose();
          },
        },
      );
    } catch (error) {
      console.error(error);
    }
  };

  const isValid =
    name.trim().length > 0 &&
    name.length <= MAX_NAME_LENGTH &&
    description.trim().length > 0 &&
    description.length <= MAX_DESCRIPTION_LENGTH;

  return (
    <Modal isOpen={isOpen} onClose={onClose} position="center" size="small">
      <Modal.Header title="그룹 정보 수정" />
      <Modal.Content>
        <S.FormContainer onSubmit={handleSubmit}>
          <S.InputGroup>
            <S.Label htmlFor="edit-group-name">그룹 이름 *</S.Label>
            <Input
              id="edit-group-name"
              type="text"
              placeholder="그룹 이름을 입력하세요"
              value={name}
              onChange={e => setName(e.target.value)}
              maxLength={MAX_NAME_LENGTH}
              required
            />
            <S.CharCount>
              {name.length} / {MAX_NAME_LENGTH}
            </S.CharCount>
          </S.InputGroup>

          <S.InputGroup>
            <S.Label htmlFor="edit-group-description">그룹 설명 *</S.Label>
            <S.TextArea
              id="edit-group-description"
              value={description}
              onChange={e => setDescription(e.target.value)}
              maxLength={MAX_DESCRIPTION_LENGTH}
              required
            />
            <S.CharCount>
              {description.length} / {MAX_DESCRIPTION_LENGTH}
            </S.CharCount>
          </S.InputGroup>

          <S.ButtonGroup>
            <Button type="button" variant="secondary" title="취소" onClick={onClose} />
            <Button
              type="submit"
              variant="primary"
              title={updateGroupMutation.isPending ? '수정 중...' : '수정하기'}
              disabled={!isValid || updateGroupMutation.isPending}
            />
          </S.ButtonGroup>
        </S.FormContainer>
      </Modal.Content>
    </Modal>
  );
};
