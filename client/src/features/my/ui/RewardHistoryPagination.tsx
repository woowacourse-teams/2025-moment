import { Button } from '@/shared/design-system/button';
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

  const handleTenPagePrevious = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 10);
    }
  };

  const handleTenPageNext = () => {
    if (currentPage < totalPages) {
      onPageChange(currentPage + 10);
    }
  };

  return (
    <S.PaginationContainer>
      <Button
        title="<<"
        onClick={handleTenPagePrevious}
        variant="quaternary"
        disabled={currentPage < 10}
      />
      <Button
        title="<"
        onClick={handlePrevious}
        variant="quaternary"
        disabled={currentPage === 0}
      />

      <S.PageInfo>
        {currentPage + 1} / {totalPages}
      </S.PageInfo>

      <Button
        title=">"
        onClick={handleNext}
        variant="quaternary"
        disabled={currentPage >= totalPages - 1}
      />
      <Button
        title=">>"
        onClick={handleTenPageNext}
        variant="quaternary"
        disabled={currentPage >= totalPages - 10}
      />
    </S.PaginationContainer>
  );
};
