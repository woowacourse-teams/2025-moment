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

export const MyGroupList = () => {
  const { data: groupsData, isLoading } = useGroupsQuery();
  const navigate = useNavigate();
  const groups = groupsData?.data || [];

  const { showSuccess, showError } = useToast();
  const deleteGroupMutation = useDeleteGroupMutation();
  const leaveGroupMutation = useLeaveGroupMutation();

  const [editingGroup, setEditingGroup] = useState<Group | null>(null);

  const handleCreateGroup = () => {
    navigate(ROUTES.GROUP_CREATE);
  };

  const handleGroupClick = (groupId: number) => {
    const path = ROUTES.TODAY_COMMENT.replace(':groupId', String(groupId));
    navigate(path);
  };

  const handleEdit = (group: Group) => {
    setEditingGroup(group);
  };

  const handleDelete = async (groupId: number) => {
    if (window.confirm('정말로 이 그룹을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      try {
        await deleteGroupMutation.mutateAsync(groupId);
        // Toast handled in mutation usually, but assuming mutation returns promise
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
    </S.Container>
  );
};
