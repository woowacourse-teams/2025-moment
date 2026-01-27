import type { GroupMoment } from "../types/moment";
import { Button } from "@shared/ui";
import * as S from "./MomentTable.styles";

interface MomentTableProps {
  moments: GroupMoment[];
  isLoading: boolean;
  isAdmin: boolean;
  onMomentClick: (momentId: number) => void;
  onDelete: (momentId: number) => void;
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
};

export function MomentTable({
  moments,
  isLoading,
  isAdmin,
  onMomentClick,
  onDelete,
}: MomentTableProps) {
  if (isLoading) {
    return <S.LoadingState>Loading...</S.LoadingState>;
  }

  if (moments.length === 0) {
    return <S.EmptyState>No moments found.</S.EmptyState>;
  }

  return (
    <S.TableContainer>
      <S.Table>
        <S.Thead>
          <tr>
            <S.Th>ID</S.Th>
            <S.Th>Author</S.Th>
            <S.Th>Content</S.Th>
            <S.Th>Comments</S.Th>
            <S.Th>Created</S.Th>
            {isAdmin && <S.Th>Actions</S.Th>}
          </tr>
        </S.Thead>
        <S.Tbody>
          {moments.map((moment) => (
            <S.Tr key={moment.id} onClick={() => onMomentClick(moment.id)}>
              <S.Td>{moment.id}</S.Td>
              <S.Td>{moment.authorNickname}</S.Td>
              <S.ContentTd>{moment.content}</S.ContentTd>
              <S.Td>{moment.commentCount}</S.Td>
              <S.Td>{formatDate(moment.createdAt)}</S.Td>
              {isAdmin && (
                <S.Td>
                  <S.ActionCell>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={(e) => {
                        e.stopPropagation();
                        onDelete(moment.id);
                      }}
                    >
                      Delete
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
