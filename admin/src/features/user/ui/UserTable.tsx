import type { User } from "../api/useUsersQuery";
import * as S from "./UserTable.styles";

interface UserTableProps {
  users: User[];
  isLoading: boolean;
  isError: boolean;
  onUserClick: (userId: number) => void;
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
};

export function UserTable({
  users,
  isLoading,
  isError,
  onUserClick,
}: UserTableProps) {
  if (isLoading) {
    return <S.LoadingState>Loading...</S.LoadingState>;
  }

  if (isError) {
    return <S.ErrorState>Failed to load users.</S.ErrorState>;
  }

  if (users.length === 0) {
    return <S.EmptyState>No users found.</S.EmptyState>;
  }

  return (
    <S.TableContainer>
      <S.Table>
        <S.Thead>
          <tr>
            <S.Th>ID</S.Th>
            <S.Th>Email</S.Th>
            <S.Th>Nickname</S.Th>
            <S.Th>Provider</S.Th>
            <S.Th>Created</S.Th>
            <S.Th>Status</S.Th>
          </tr>
        </S.Thead>
        <S.Tbody>
          {users.map((user) => (
            <S.Tr key={user.id} onClick={() => onUserClick(user.id)}>
              <S.Td>{user.id}</S.Td>
              <S.Td>{user.email}</S.Td>
              <S.Td>{user.nickname}</S.Td>
              <S.Td>{user.providerType}</S.Td>
              <S.Td>{formatDate(user.createdAt)}</S.Td>
              <S.Td>
                <S.Badge $variant={user.deletedAt ? "deleted" : "active"}>
                  {user.deletedAt ? "Deleted" : "Active"}
                </S.Badge>
              </S.Td>
            </S.Tr>
          ))}
        </S.Tbody>
      </S.Table>
    </S.TableContainer>
  );
}
