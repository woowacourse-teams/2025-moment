import { useState } from 'react';
import { Modal } from '@/shared/design-system/modal/Modal';
import { Button } from '@/shared/design-system/button/Button';
import { Input } from '@/shared/design-system/input/Input';
import { useJoinGroupMutation } from '../api/useJoinGroupMutation';
import { useNavigate } from 'react-router';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { ROUTES } from '@/app/routes/routes';
import * as S from './GroupSelectionModal.styles';
import { useEffect } from 'react';

interface GroupSelectionModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function GroupSelectionModal({ isOpen, onClose }: GroupSelectionModalProps) {
  const [showInviteInput, setShowInviteInput] = useState(false);
  const [inviteCode, setInviteCode] = useState('');
  const [nickname, setNickname] = useState('');
  const navigate = useNavigate();
  const joinGroupMutation = useJoinGroupMutation();

  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  const { data: profile } = useProfileQuery({ enabled: !!isLoggedIn });

  useEffect(() => {
    if (profile?.nickname && !nickname) {
      setNickname(profile.nickname);
    }
  }, [profile?.nickname, showInviteInput]);

  const handleCreateGroup = () => {
    navigate(ROUTES.GROUP_CREATE);
    onClose();
  };

  const handleJoinGroup = async () => {
    if (!inviteCode.trim() || !nickname.trim()) return;

    try {
      await joinGroupMutation.mutateAsync({ inviteCode, nickname });
      onClose();
      navigate(ROUTES.GROUPS);
    } catch (error) {
      console.error('Failed to join group:', error);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} size="small" titleId="group-selection-title">
      <Modal.Header
        title="그룹을 선택해주세요"
        showCloseButton={false}
        id="group-selection-title"
      />
      <Modal.Content>
        <S.ModalContent>
          <S.Description>
            모멘트를 사용하려면 그룹이 필요합니다.
            <br />
            새로운 그룹을 만들거나 초대 코드로 그룹에 참여하세요.
          </S.Description>

          <S.ButtonGroup>
            <Button
              title="새 그룹 만들기"
              variant="primary"
              onClick={handleCreateGroup}
              aria-label="새 그룹 만들기"
            />

            {!showInviteInput ? (
              <>
                <S.Divider>
                  <S.DividerText>또는</S.DividerText>
                </S.Divider>
                <Button
                  title="초대 코드로 참여하기"
                  variant="secondary"
                  onClick={() => setShowInviteInput(true)}
                  aria-label="초대 코드 입력 화면 열기"
                />
              </>
            ) : (
              <S.InviteCodeInput>
                <S.Label htmlFor="invite-code">초대 코드</S.Label>
                <Input
                  id="invite-code"
                  type="text"
                  placeholder="초대 코드를 입력하세요"
                  value={inviteCode}
                  onChange={e => setInviteCode(e.target.value)}
                  aria-label="초대 코드 입력"
                />
                <S.Label htmlFor="nickname">사용할 닉네임</S.Label>
                <Input
                  id="nickname"
                  type="text"
                  placeholder="닉네임을 입력하세요"
                  value={nickname}
                  onChange={e => setNickname(e.target.value)}
                  aria-label="닉네임 입력"
                />
                <Button
                  title="참여하기"
                  variant="primary"
                  onClick={handleJoinGroup}
                  disabled={!inviteCode.trim() || !nickname.trim() || joinGroupMutation.isPending}
                  aria-label="그룹 참여하기"
                />
                <Button
                  title="취소"
                  variant="secondary"
                  onClick={() => {
                    setShowInviteInput(false);
                    setInviteCode('');
                  }}
                  aria-label="취소"
                />
              </S.InviteCodeInput>
            )}
          </S.ButtonGroup>
        </S.ModalContent>
      </Modal.Content>
    </Modal>
  );
}
