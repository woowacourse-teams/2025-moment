import { useState } from 'react';
import { Button } from '@/shared/design-system/button/Button';
import { useCreateInviteMutation } from '../api/useCreateInviteMutation';
import { useToast } from '@/shared/hooks/useToast';
import * as S from './GroupInviteSection.styles';

interface GroupInviteSectionProps {
  groupId: number | string;
  showTitle?: boolean;
  showContainer?: boolean;
}

export function GroupInviteSection({
  groupId,
  showTitle = true,
  showContainer = true,
}: GroupInviteSectionProps) {
  const [inviteCode, setInviteCode] = useState<string | null>(null);
  const createInviteMutation = useCreateInviteMutation(groupId);
  const { showSuccess } = useToast();

  const handleGenerateCode = async () => {
    try {
      const result = await createInviteMutation.mutateAsync();
      setInviteCode(result.data);
    } catch (error) {
      console.error('Failed to generate invite code:', error);
    }
  };

  const handleCopyCode = () => {
    if (inviteCode) {
      const inviteUrl = `${window.location.origin}/invite/${inviteCode}`;
      navigator.clipboard.writeText(inviteUrl);
      showSuccess('초대 링크가 복사되었습니다!');
    }
  };

  const Content = (
    <>
      {showTitle && <S.Title>그룹 초대</S.Title>}
      <S.Description>
        초대 코드를 생성하여 친구들을 그룹에 초대하세요.
        <br />
        초대받은 사람은 승인 후 그룹에 참여할 수 있습니다.
      </S.Description>

      {inviteCode ? (
        <>
          <S.InviteCodeContainer>
            <S.CodeDisplay>{inviteCode}</S.CodeDisplay>
          </S.InviteCodeContainer>
          <S.ButtonGroup>
            <Button
              title="링크 복사"
              variant="primary"
              onClick={handleCopyCode}
              aria-label="초대 링크 복사"
            />
            <Button
              title="새 코드 생성"
              variant="secondary"
              onClick={handleGenerateCode}
              disabled={createInviteMutation.isPending}
              aria-label="새 초대 코드 생성"
            />
          </S.ButtonGroup>
        </>
      ) : (
        <Button
          title="초대 코드 생성"
          variant="primary"
          onClick={handleGenerateCode}
          disabled={createInviteMutation.isPending}
          aria-label="초대 코드 생성"
        />
      )}
    </>
  );

  if (!showContainer) {
    return Content;
  }

  return <S.Container>{Content}</S.Container>;
}
