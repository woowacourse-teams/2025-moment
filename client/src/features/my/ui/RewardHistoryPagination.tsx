import { Button } from '@/shared/ui';
import * as S from './RewardHistoryPagination.styles';

interface RewardHistoryPaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export const RewardHistoryPagination = ({
  currentPage,
  totalPages,
  onPageChange,
}: RewardHistoryPaginationProps) => {
  const handlePrevious = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages) {
      onPageChange(currentPage + 1);
    }
  };

  return (
    <S.PaginationContainer>
      {currentPage > 0 ? (
        <Button title="이전" onClick={handlePrevious} variant="primary" />
      ) : (
        <Button title="이전" onClick={handlePrevious} variant="primary" disabled />
      )}

      <S.PageInfo>
        {currentPage + 1} / {totalPages}
      </S.PageInfo>

      {currentPage < totalPages - 1 ? (
        <Button title="다음" onClick={handleNext} variant="primary" />
      ) : (
        <Button title="다음" onClick={handleNext} variant="primary" disabled />
      )}
    </S.PaginationContainer>
  );
};
