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
    if (currentPage > 1) {
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
      <Button title="이전" onClick={handlePrevious} disabled={currentPage <= 1} variant="primary" />

      <S.PageInfo>
        {currentPage} / {totalPages}
      </S.PageInfo>

      <Button
        title="다음"
        onClick={handleNext}
        disabled={currentPage >= totalPages}
        variant="primary"
      />
    </S.PaginationContainer>
  );
};
