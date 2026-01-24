import { Input } from '@/shared/design-system/input/Input';
import { Button } from '@/shared/design-system/button/Button';
import { useGroupCreateForm } from '../hooks/useGroupCreateForm';
import * as S from './GroupCreateForm.styles';

interface GroupCreateFormProps {
  onSuccess?: () => void;
  onCancel?: () => void;
}

export function GroupCreateForm({ onSuccess, onCancel }: GroupCreateFormProps) {
  const {
    name,
    description,
    profile,
    setName,
    setDescription,
    isValid,
    isPending,
    MAX_NAME_LENGTH,
    MAX_DESCRIPTION_LENGTH,
    handleSubmit,
  } = useGroupCreateForm({ onSuccess });

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
        <S.Label htmlFor="owner-nickname">그룹장 닉네임</S.Label>
        <Input
          id="owner-nickname"
          type="text"
          placeholder="프로필 닉네임이 자동으로 설정됩니다"
          value={profile?.nickname || ''}
          readOnly
          disabled
          aria-label="그룹장 닉네임"
        />
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="group-description">그룹 설명 *</S.Label>
        <S.TextArea
          id="group-description"
          placeholder="그룹에 대한 설명을 입력하세요"
          value={description}
          onChange={e => setDescription(e.target.value)}
          maxLength={MAX_DESCRIPTION_LENGTH}
          required
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
          disabled={!isValid || isPending}
          aria-label="그룹 만들기"
        />
      </S.ButtonGroup>
    </S.FormContainer>
  );
}
