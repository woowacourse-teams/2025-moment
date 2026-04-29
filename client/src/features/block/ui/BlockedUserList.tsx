import { useBlockListQuery } from '../api/useBlockListQuery';
import { useUnblockMutation } from '../api/useUnblockMutation';
import { Button } from '@/shared/design-system/button';
import * as S from './BlockedUserList.styles';

export const BlockedUserList = () => {
  const { data: blockedUsers, isLoading } = useBlockListQuery();
  const unblockMutation = useUnblockMutation();

  const handleUnblock = (userId: number) => {
    unblockMutation.mutate(userId);
  };

  if (isLoading) {
    return (
      <S.Container>
        <S.Header>
          <S.Title>차단 목록</S.Title>
        </S.Header>
        <p>로딩 중...</p>
      </S.Container>
    );
  }

  return (
    <S.Container>
      <S.Header>
        <S.Title>차단 목록 ({blockedUsers?.length ?? 0})</S.Title>
      </S.Header>
      {!blockedUsers || blockedUsers.length === 0 ? (
        <S.EmptyState>
          <p>차단한 사용자가 없습니다.</p>
        </S.EmptyState>
      ) : (
        <S.List>
          {blockedUsers.map(user => (
            <S.UserItem key={user.blockedUserId}>
              <S.Nickname>{user.nickname}</S.Nickname>
              <Button
                variant="danger"
                onClick={() => handleUnblock(user.blockedUserId)}
                disabled={unblockMutation.isPending}
              >
                차단 해제
              </Button>
            </S.UserItem>
          ))}
        </S.List>
      )}
    </S.Container>
  );
};
