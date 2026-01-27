import type { PendingMember } from "../types/member";
import { Button } from "@shared/ui";
import * as S from "./PendingMemberTable.styles";

interface PendingMemberTableProps {
  pendingMembers: PendingMember[];
  isLoading: boolean;
  isAdmin: boolean;
  onApprove: (memberId: number) => void;
  onReject: (memberId: number) => void;
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
};

export function PendingMemberTable({
  pendingMembers,
  isLoading,
  isAdmin,
  onApprove,
  onReject,
}: PendingMemberTableProps) {
  if (isLoading) {
    return <S.LoadingState>Loading...</S.LoadingState>;
  }

  if (pendingMembers.length === 0) {
    return <S.EmptyState>No pending members.</S.EmptyState>;
  }

  return (
    <S.TableContainer>
      <S.Table>
        <S.Thead>
          <tr>
            <S.Th>User ID</S.Th>
            <S.Th>Nickname</S.Th>
            <S.Th>Requested</S.Th>
            {isAdmin && <S.Th>Actions</S.Th>}
          </tr>
        </S.Thead>
        <S.Tbody>
          {pendingMembers.map((member) => (
            <S.Tr key={member.id}>
              <S.Td>{member.userId}</S.Td>
              <S.Td>{member.nickname}</S.Td>
              <S.Td>{formatDate(member.requestedAt)}</S.Td>
              {isAdmin && (
                <S.Td>
                  <S.ActionCell>
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => onApprove(member.id)}
                    >
                      Approve
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => onReject(member.id)}
                    >
                      Reject
                    </Button>
                  </S.ActionCell>
                </S.Td>
              )}
            </S.Tr>
          ))}
        </S.Tbody>
      </S.Table>
    </S.TableContainer>
  );
}
