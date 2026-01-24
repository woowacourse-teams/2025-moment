import { useGroupsQuery } from '../api/useGroupsQuery';
import { GroupCard } from './GroupCard';
import { Button } from '@/shared/design-system/button/Button';
import { useNavigate } from 'react-router';
import { ROUTES } from '@/app/routes/routes';
import * as S from './MyGroupList.styles';
import { useDeleteGroupMutation } from '../api/useDeleteGroupMutation';
import { useLeaveGroupMutation } from '../api/useLeaveGroupMutation';
import { useToast } from '@/shared/hooks/useToast';
import { useState } from 'react';
import { Group } from '../types/group';
import { EditGroupModal } from './EditGroupModal';
import { GroupMemberManagementModal } from './GroupMemberManagementModal';
import { EditGroupProfileModal } from './EditGroupProfileModal';
import { useModal } from '@/shared/design-system/modal';
import { Modal } from '@/shared/design-system/modal/Modal';
import { GroupCreateForm } from './GroupCreateForm';
import { GroupCreateSuccess } from './GroupCreateSuccess';
import { GroupInviteSection } from './GroupInviteSection';

export const MyGroupList = () => {
  const { data: groupsData, isLoading } = useGroupsQuery();
  const navigate = useNavigate();
  const groups = groupsData?.data || [];

  const { showSuccess, showError } = useToast();
  const deleteGroupMutation = useDeleteGroupMutation();
  const leaveGroupMutation = useLeaveGroupMutation();

  const [editingGroup, setEditingGroup] = useState<Group | null>(null);
  const [managingGroupId, setManagingGroupId] = useState<number | null>(null);
  const [editingProfileGroup, setEditingProfileGroup] = useState<Group | null>(null);
  const [invitingGroupId, setInvitingGroupId] = useState<number | null>(null);

  const {
    isOpen: isCreateOpen,
    handleOpen: openCreateModal,
    handleClose: closeCreateModal,
  } = useModal();
  const {
    isOpen: isSuccessOpen,
    handleOpen: openSuccessModal,
    handleClose: closeSuccessModal,
  } = useModal();
  const [createdGroupInfo, setCreatedGroupInfo] = useState<{
    groupId: number;
    code: string;
  } | null>(null);

  const handleCreateGroup = () => {
    openCreateModal();
  };

  const handleCreateSuccess = (groupId: number, code: string) => {
    setCreatedGroupInfo({ groupId, code });
    closeCreateModal();
    openSuccessModal();
  };

  const handleGroupClick = (groupId: number) => {
    const path = ROUTES.TODAY_COMMENT.replace(':groupId', String(groupId));
    navigate(path);
  };

  const handleEdit = (group: Group) => {
    setEditingGroup(group);
  };

  const handleManageMembers = (groupId: number) => {
    setManagingGroupId(groupId);
  };

  const handleEditProfile = (group: Group) => {
    setEditingProfileGroup(group);
  };

  const handleInvite = (groupId: number) => {
    setInvitingGroupId(groupId);
  };

  const handleDelete = async (groupId: number) => {
    if (window.confirm('정말로 이 그룹을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      try {
        await deleteGroupMutation.mutateAsync(groupId);
      } catch (error) {
        console.error('Failed to delete group', error);
      }
    }
  };

  const handleLeave = async (groupId: number) => {
    if (window.confirm('정말로 이 그룹을 탈퇴하시겠습니까?')) {
      try {
        await leaveGroupMutation.mutateAsync(groupId);
      } catch (error) {
        console.error('Failed to leave group', error);
      }
    }
  };

  if (isLoading) {
    return (
      <S.Container>
        <S.Header>
          <S.Title>내 그룹</S.Title>
        </S.Header>
        <p>로딩 중...</p>
      </S.Container>
    );
  }

  return (
    <S.Container>
      <S.Header>
        <S.Title>내 그룹 ({groups.length})</S.Title>
        <Button variant="primary" title="그룹 생성" onClick={handleCreateGroup} />
      </S.Header>
      {groups.length === 0 ? (
        <S.EmptyState>
          <p>아직 참여한 그룹이 없습니다.</p>
        </S.EmptyState>
      ) : (
        <S.Grid>
          {groups.map(group => (
            <GroupCard
              key={group.groupId}
              group={group}
              onClick={() => handleGroupClick(group.groupId)}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onLeave={handleLeave}
              onManageMembers={handleManageMembers}
              onEditProfile={handleEditProfile}
              onInvite={handleInvite}
            />
          ))}
        </S.Grid>
      )}

      {editingGroup && (
        <EditGroupModal
          group={editingGroup}
          isOpen={!!editingGroup}
          onClose={() => setEditingGroup(null)}
        />
      )}

      {managingGroupId && (
        <GroupMemberManagementModal
          groupId={managingGroupId}
          isOpen={!!managingGroupId}
          onClose={() => setManagingGroupId(null)}
        />
      )}

      {editingProfileGroup && (
        <EditGroupProfileModal
          groupId={editingProfileGroup.groupId}
          currentNickname={editingProfileGroup.myNickname}
          isOpen={!!editingProfileGroup}
          onClose={() => setEditingProfileGroup(null)}
        />
      )}

      <Modal isOpen={isCreateOpen} onClose={closeCreateModal}>
        <Modal.Header title="그룹 생성" showCloseButton />
        <Modal.Content>
          <GroupCreateForm onSuccess={handleCreateSuccess} onCancel={closeCreateModal} />
        </Modal.Content>
      </Modal>

      <Modal isOpen={isSuccessOpen} onClose={closeSuccessModal}>
        <Modal.Header title="참여 코드" showCloseButton />
        <Modal.Content>
          {createdGroupInfo && (
            <GroupCreateSuccess
              groupId={createdGroupInfo.groupId}
              inviteCode={createdGroupInfo.code}
              onClose={closeSuccessModal}
            />
          )}
        </Modal.Content>
      </Modal>

      <Modal isOpen={!!invitingGroupId} onClose={() => setInvitingGroupId(null)}>
        <Modal.Header title="그룹 초대" showCloseButton />
        <Modal.Content>
          {invitingGroupId && (
            <GroupInviteSection groupId={invitingGroupId} showTitle={false} showContainer={false} />
          )}
        </Modal.Content>
      </Modal>
    </S.Container>
  );
};
