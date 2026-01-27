import { useState } from 'react';
import { Modal } from '@/shared/design-system/modal/Modal';
import { Button } from '@/shared/design-system/button/Button';
import { Input } from '@/shared/design-system/input/Input';
import { useUpdateProfileMutation } from '../api/useUpdateProfileMutation';
import styled from '@emotion/styled';
import { theme } from '@/shared/styles/theme';

interface EditGroupProfileModalProps {
  groupId: number | string;
  currentNickname: string;
  isOpen: boolean;
  onClose: () => void;
}

export const EditGroupProfileModal = ({
  groupId,
  currentNickname,
  isOpen,
  onClose,
}: EditGroupProfileModalProps) => {
  const [nickname, setNickname] = useState(currentNickname);
  const updateProfileMutation = useUpdateProfileMutation(groupId);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!nickname.trim()) return;

    updateProfileMutation.mutate(
      { nickname: nickname.trim() },
      {
        onSuccess: () => {
          onClose();
        },
      },
    );
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} size="small">
      <Modal.Header title="그룹 프로필 수정" />
      <Modal.Content>
        <Form onSubmit={handleSubmit}>
          <FormGroup>
            <Label htmlFor="group-nickname">그룹 내 닉네임</Label>
            <Input
              id="group-nickname"
              type="text"
              value={nickname}
              onChange={e => setNickname(e.target.value)}
              placeholder="변경할 닉네임을 입력하세요"
              required
            />
            <HelperText>그룹마다 서로 다른 닉네임을 사용할 수 있습니다.</HelperText>
          </FormGroup>
          <ButtonGroup>
            <Button variant="secondary" title="취소" onClick={onClose} type="button" />
            <Button
              variant="primary"
              title={updateProfileMutation.isPending ? '수정 중...' : '수정하기'}
              disabled={!nickname.trim() || updateProfileMutation.isPending}
              type="submit"
            />
          </ButtonGroup>
        </Form>
      </Modal.Content>
    </Modal>
  );
};

const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

const FormGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

const Label = styled.label`
  font-size: 14px;
  font-weight: 500;
  color: ${theme.colors['gray-700']};
`;

const HelperText = styled.p`
  font-size: 12px;
  color: ${theme.colors['gray-400']};
`;

const ButtonGroup = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 12px;
`;
