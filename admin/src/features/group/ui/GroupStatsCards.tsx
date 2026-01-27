import { useGroupStatsQuery } from "../api/useGroupStatsQuery";
import * as S from "./GroupStatsCards.styles";

export function GroupStatsCards() {
  const { data: stats, isLoading, isError } = useGroupStatsQuery();

  if (isLoading) {
    return (
      <S.Grid>
        <S.Card>
          <S.CardLabel>Loading...</S.CardLabel>
        </S.Card>
      </S.Grid>
    );
  }

  if (isError || !stats) {
    return null;
  }

  return (
    <S.Grid>
      <S.Card>
        <S.CardLabel>Total Groups</S.CardLabel>
        <S.CardValue>{stats.totalGroups}</S.CardValue>
      </S.Card>
      <S.Card>
        <S.CardLabel>Active Groups</S.CardLabel>
        <S.CardValue>{stats.activeGroups}</S.CardValue>
      </S.Card>
      <S.Card>
        <S.CardLabel>Deleted Groups</S.CardLabel>
        <S.CardValue>{stats.deletedGroups}</S.CardValue>
      </S.Card>
      <S.Card>
        <S.CardLabel>Total Members</S.CardLabel>
        <S.CardValue>{stats.totalMembers}</S.CardValue>
      </S.Card>
    </S.Grid>
  );
}
