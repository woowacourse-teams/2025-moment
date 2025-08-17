import { RewardHistoryItem } from '../types/rewardHistory';
import * as S from './RewardHistoryTable.styles';

interface RewardHistoryTableProps {
  items: RewardHistoryItem[];
}

const getReasonText = (reason: string) => {
  const reasonMap: Record<string, string> = {
    COMMENT_CREATION: '댓글 작성',
    MOMENT_CREATION: '모멘트 작성',
    DAILY_LOGIN: '일일 로그인',
    PURCHASE: '구매',
  };
  return reasonMap[reason] || reason;
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const RewardHistoryTable = ({ items }: RewardHistoryTableProps) => {
  return (
    <S.TableContainer>
      <S.Table>
        <S.TableHeader>
          <S.HeaderRow>
            <S.HeaderCell>날짜</S.HeaderCell>
            <S.HeaderCell>사유</S.HeaderCell>
            <S.HeaderCell>변경량</S.HeaderCell>
          </S.HeaderRow>
        </S.TableHeader>
        <S.TableBody>
          {items.map(item => (
            <S.BodyRow key={item.id}>
              <S.BodyCell>{formatDate(item.createdAt)}</S.BodyCell>
              <S.BodyCell>{getReasonText(item.reason)}</S.BodyCell>
              <S.BodyCell $isPositive={item.changeStar > 0}>
                {item.changeStar > 0 ? '+' : ''}
                {item.changeStar}
              </S.BodyCell>
            </S.BodyRow>
          ))}
        </S.TableBody>
      </S.Table>
    </S.TableContainer>
  );
};
