import type { GroupComment } from '../types/comment';
import { Button } from '@shared/ui';
import * as S from './CommentTable.styles';

interface CommentTableProps {
  comments: GroupComment[];
  isLoading: boolean;
  isAdmin: boolean;
  onDelete: (commentId: number) => void;
  onBack: () => void;
  momentId: string;
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

export function CommentTable({
  comments,
  isLoading,
  isAdmin,
  onDelete,
  onBack,
  momentId,
}: CommentTableProps) {
  if (isLoading) {
    return <S.LoadingState>Loading...</S.LoadingState>;
  }

  return (
    <div>
      <S.BackButton onClick={onBack}>&larr; Back to moments</S.BackButton>
      <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
        Comments for Moment #{momentId}
      </p>

      {comments.length === 0 ? (
        <S.EmptyState>No comments found.</S.EmptyState>
      ) : (
        <S.TableContainer>
          <S.Table>
            <S.Thead>
              <tr>
                <S.Th>ID</S.Th>
                <S.Th>Author</S.Th>
                <S.Th>Content</S.Th>
                <S.Th>Created</S.Th>
                {isAdmin && <S.Th>Actions</S.Th>}
              </tr>
            </S.Thead>
            <S.Tbody>
              {comments.map((comment) => (
                <S.Tr key={comment.id}>
                  <S.Td>{comment.id}</S.Td>
                  <S.Td>{comment.authorNickname}</S.Td>
                  <S.ContentTd>{comment.content}</S.ContentTd>
                  <S.Td>{formatDate(comment.createdAt)}</S.Td>
                  {isAdmin && (
                    <S.Td>
                      <S.ActionCell>
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => onDelete(comment.id)}
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
      )}
    </div>
  );
}
