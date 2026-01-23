import { useState } from 'react';
import { Input } from '@/shared/design-system/input/Input';
import { Button } from '@/shared/design-system/button/Button';
import { useCreateGroupMutation } from '../api/useCreateGroupMutation';
import * as S from './GroupCreateForm.styles';

interface GroupCreateFormProps {
  onSuccess?: () => void;
  onCancel?: () => void;
}

const MAX_NAME_LENGTH = 50;
const MAX_DESCRIPTION_LENGTH = 200;

export function GroupCreateForm({ onSuccess, onCancel }: GroupCreateFormProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const createGroupMutation = useCreateGroupMutation();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim()) return;

    try {
      await createGroupMutation.mutateAsync({
        name: name.trim(),
        description: description.trim(),
      });
      onSuccess?.();
    } catch (error) {
      console.error('Failed to create group:', error);
    }
  };

  const isValid = name.trim().length > 0 && name.length <= MAX_NAME_LENGTH;

  return (
    <S.FormContainer onSubmit={handleSubmit}>
      <S.InputGroup>
        <S.Label htmlFor="group-name">그룹 이름 *</S.Label>
        <Input
          id="group-name"
          type="text"
          placeholder="그룹 이름을 입력하세요"
          value={name}
          onChange={e => setName(e.target.value)}
          maxLength={MAX_NAME_LENGTH}
          required
          aria-label="그룹 이름"
        />
        <S.CharCount>
          {name.length} / {MAX_NAME_LENGTH}
        </S.CharCount>
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="group-description">그룹 설명</S.Label>
        <S.TextArea
          id="group-description"
          placeholder="그룹에 대한 설명을 입력하세요 (선택사항)"
          value={description}
          onChange={e => setDescription(e.target.value)}
          maxLength={MAX_DESCRIPTION_LENGTH}
          aria-label="그룹 설명"
        />
        <S.CharCount>
          {description.length} / {MAX_DESCRIPTION_LENGTH}
        </S.CharCount>
      </S.InputGroup>

      <S.ButtonGroup>
        {onCancel && (
          <Button
            type="button"
            title="취소"
            variant="secondary"
            onClick={onCancel}
            aria-label="취소"
          />
        )}
        <Button
          type="submit"
          title="그룹 만들기"
          variant="primary"
          disabled={!isValid || createGroupMutation.isPending}
          aria-label="그룹 만들기"
        />
      </S.ButtonGroup>
    </S.FormContainer>
  );
}
