import { useGroupsQuery } from '../api/useGroupsQuery';
import { useCurrentGroup } from '../hooks/useCurrentGroup';
import { useNavigate } from 'react-router';
import { GroupCard } from './GroupCard';
import { Button } from '@/shared/design-system/button/Button';
import * as S from './GroupList.styles';

export function GroupList() {
  const { data: groupsData, isLoading } = useGroupsQuery();
  const { selectGroup } = useCurrentGroup();
  const navigate = useNavigate();

  const handleGroupClick = (group: any) => {
    selectGroup(group);
    navigate(`/groups/${group.id}`);
  };

  const handleCreateGroup = () => {
    navigate('/groups/create');
  };

  if (isLoading) {
    return (
      <S.LoadingState>
        <p>로딩 중...</p>
      </S.LoadingState>
    );
  }

  const groups = groupsData?.data || [];

  if (groups.length === 0) {
    return (
      <S.EmptyState>
        <S.EmptyText>아직 참여한 그룹이 없습니다.</S.EmptyText>
        <Button title="새 그룹 만들기" variant="primary" onClick={handleCreateGroup} />
      </S.EmptyState>
    );
  }

  return (
    <S.ListContainer>
      {groups.map(group => (
        <GroupCard key={group.id} group={group} onClick={() => handleGroupClick(group)} />
      ))}
    </S.ListContainer>
  );
}
