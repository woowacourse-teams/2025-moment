import { Input } from '@/shared/design-system/input';
import { Button } from '@/shared/design-system/button';
import { useNavigate } from 'react-router';
import { toast } from '@/shared/store/toast';
import * as S from './GroupCreateForm.styles';

interface GroupCreateSuccessProps {
  inviteCode: string;
  groupId: number;
  onClose: () => void;
}

export function GroupCreateSuccess({ inviteCode, groupId, onClose }: GroupCreateSuccessProps) {
  const navigate = useNavigate();

  const inviteLink = `${window.location.origin}/invite/${inviteCode}`;

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(inviteLink);
      toast.success('초대 링크가 복사되었습니다!');
    } catch (err) {
      console.error('Failed to copy invite link', err);
    }
  };

  const handleGoToGroup = () => {
    onClose();
    navigate(`/groups/${groupId}/today-comment`);
  };

  return (
    <S.FormContainer>
      <S.SectionTitle>그룹이 생성되었습니다! 🎉</S.SectionTitle>
      <S.Description>멤버들을 초대해보세요.</S.Description>

      <S.InputGroup>
        <S.Label htmlFor="invite-link">초대 링크</S.Label>
        <div style={{ display: 'flex', gap: '8px' }}>
          <Input
            id="invite-link"
            type="text"
            value={inviteLink}
            readOnly
            placeholder="초대 링크"
            aria-label="초대 링크"
          />
          <Button
            type="button"
            title="복사"
            variant="secondary"
            onClick={handleCopyLink}
            style={{ width: '80px', whiteSpace: 'nowrap' }}
          />
        </div>
      </S.InputGroup>

      <S.ButtonGroup>
        <Button variant="primary" onClick={handleGoToGroup}>그룹으로 이동</Button>
      </S.ButtonGroup>
    </S.FormContainer>
  );
}
