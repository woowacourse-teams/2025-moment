import { useState } from 'react';
import { Modal } from '@/shared/design-system/modal/Modal';
import { Button } from '@/shared/design-system/button/Button';
import { Input } from '@/shared/design-system/input/Input';
import { useJoinGroupMutation } from '../api/useJoinGroupMutation';
import { useNavigate } from 'react-router';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { ROUTES } from '@/app/routes/routes';
import { GroupCreateForm } from './GroupCreateForm';
import { GroupCreateSuccess } from './GroupCreateSuccess';
import * as S from './GroupSelectionModal.styles';
import { useEffect } from 'react';

interface GroupSelectionModalProps {
  isOpen: boolean;
  onClose: () => void;
}

type ModalView = 'selection' | 'create' | 'join' | 'success';

export function GroupSelectionModal({ isOpen, onClose }: GroupSelectionModalProps) {
  const [modalView, setModalView] = useState<ModalView>('selection');
  const [createdGroupInfo, setCreatedGroupInfo] = useState<{
    groupId: number;
    code: string;
  } | null>(null);
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
  }, [profile?.nickname, modalView]);

  const handleCreateGroup = () => {
    setModalView('create');
  };

  const handleCreateSuccess = (groupId: number, code: string) => {
    setCreatedGroupInfo({ groupId, code });
    setModalView('success');
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
    <Modal
      isOpen={isOpen}
      onClose={modalView === 'selection' ? onClose : () => setModalView('selection')}
      size="small"
      titleId="group-selection-title"
    >
      <Modal.Header
        title={
          modalView === 'selection'
            ? '그룹을 선택해주세요'
            : modalView === 'create'
              ? '그룹 생성'
              : modalView === 'join'
                ? '그룹 참여'
                : '참여 코드'
        }
        showCloseButton={modalView !== 'selection'}
        id="group-selection-title"
      />
      <Modal.Content>
        <S.ModalContent>
          {modalView === 'selection' && (
            <>
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
                <S.Divider>
                  <S.DividerText>또는</S.DividerText>
                </S.Divider>
                <Button
                  title="초대 코드로 참여하기"
                  variant="secondary"
                  onClick={() => setModalView('join')}
                  aria-label="초대 코드 입력 화면 열기"
                />
              </S.ButtonGroup>
            </>
          )}

          {modalView === 'create' && (
            <GroupCreateForm
              onSuccess={handleCreateSuccess}
              onCancel={() => setModalView('selection')}
            />
          )}

          {modalView === 'join' && (
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
                  setModalView('selection');
                  setInviteCode('');
                }}
                aria-label="취소"
              />
            </S.InviteCodeInput>
          )}

          {modalView === 'success' && createdGroupInfo && (
            <GroupCreateSuccess
              groupId={createdGroupInfo.groupId}
              inviteCode={createdGroupInfo.code}
              onClose={onClose}
            />
          )}
        </S.ModalContent>
      </Modal.Content>
    </Modal>
  );
}
