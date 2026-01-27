import type { Group } from '../types/group';
import * as S from './GroupTable.styles';

interface GroupTableProps {
  groups: Group[];
  isLoading: boolean;
  isError: boolean;
  onGroupClick: (groupId: number) => void;
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

export function GroupTable({ groups, isLoading, isError, onGroupClick }: GroupTableProps) {
  if (isLoading) {
    return <S.LoadingState>Loading...</S.LoadingState>;
  }

  if (isError) {
    return <S.ErrorState>Failed to load groups.</S.ErrorState>;
  }

  if (groups.length === 0) {
    return <S.EmptyState>No groups found.</S.EmptyState>;
  }

  return (
    <S.TableContainer>
      <S.Table>
        <S.Thead>
          <tr>
            <S.Th>ID</S.Th>
            <S.Th>Name</S.Th>
            <S.Th>Description</S.Th>
            <S.Th>Owner</S.Th>
            <S.Th>Members</S.Th>
            <S.Th>Moments</S.Th>
            <S.Th>Created</S.Th>
            <S.Th>Status</S.Th>
          </tr>
        </S.Thead>
        <S.Tbody>
          {groups.map((group) => (
            <S.Tr key={group.id} onClick={() => onGroupClick(group.id)}>
              <S.Td>{group.id}</S.Td>
              <S.Td>{group.name}</S.Td>
              <S.DescriptionTd>{group.description}</S.DescriptionTd>
              <S.Td>{group.ownerNickname}</S.Td>
              <S.Td>{group.memberCount}</S.Td>
              <S.Td>{group.momentCount}</S.Td>
              <S.Td>{formatDate(group.createdAt)}</S.Td>
              <S.Td>
                <S.Badge $variant={group.status === 'DELETED' ? 'deleted' : 'active'}>
                  {group.status === 'DELETED' ? 'Deleted' : 'Active'}
                </S.Badge>
              </S.Td>
            </S.Tr>
          ))}
        </S.Tbody>
      </S.Table>
    </S.TableContainer>
  );
}
