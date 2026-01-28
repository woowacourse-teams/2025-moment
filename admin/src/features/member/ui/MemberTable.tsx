import type { Member } from "../types/member";
import { Button } from "@shared/ui";
import * as S from "./MemberTable.styles";

interface MemberTableProps {
  members: Member[];
  isLoading: boolean;
  isAdmin: boolean;
  onKick: (memberId: number) => void;
  onTransfer: (memberId: number) => void;
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
};

export function MemberTable({
  members,
  isLoading,
  isAdmin,
  onKick,
  onTransfer,
}: MemberTableProps) {
  if (isLoading) {
    return <S.LoadingState>Loading...</S.LoadingState>;
  }

  if (members.length === 0) {
    return <S.EmptyState>No members found.</S.EmptyState>;
  }

  return (
    <S.TableContainer>
      <S.Table>
        <S.Thead>
          <tr>
            <S.Th>ID</S.Th>
            <S.Th>Nickname</S.Th>
            <S.Th>Role</S.Th>
            <S.Th>Joined</S.Th>
            {isAdmin && <S.Th>Actions</S.Th>}
          </tr>
        </S.Thead>
        <S.Tbody>
          {members.map((member) => (
            <S.Tr key={member.id}>
              <S.Td>{member.userId}</S.Td>
              <S.Td>{member.nickname}</S.Td>
              <S.Td>
                <S.RoleBadge $role={member.role}>{member.role}</S.RoleBadge>
              </S.Td>
              <S.Td>{formatDate(member.joinedAt)}</S.Td>
              {isAdmin && (
                <S.Td>
                  {member.role !== "OWNER" && (
                    <S.ActionCell>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => onKick(member.id)}
                      >
                        Kick
                      </Button>
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => onTransfer(member.id)}
                      >
                        Transfer
                      </Button>
                    </S.ActionCell>
                  )}
                </S.Td>
              )}
            </S.Tr>
          ))}
        </S.Tbody>
      </S.Table>
    </S.TableContainer>
  );
}
