import { RewardHistoryItem } from '../types/rewardHistory';
import * as S from './RewardHistoryTable.styles';
import { formatDate, getReasonText } from '../utils/rewardHistoryTableHelper';

interface RewardHistoryTableProps {
  items: RewardHistoryItem[];
}

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
        <tbody>
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
        </tbody>
      </S.Table>
    </S.TableContainer>
  );
};
