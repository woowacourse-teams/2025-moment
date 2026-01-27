import * as S from './Pagination.styles';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  const getPageNumbers = () => {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
    const end = Math.min(totalPages, start + maxVisible);

    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }

    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  };

  return (
    <S.PaginationContainer>
      <S.PageButton
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
        aria-label="Previous page"
      >
        &lt;
      </S.PageButton>

      {getPageNumbers().map((page) => (
        <S.PageButton
          key={page}
          $active={page === currentPage}
          onClick={() => onPageChange(page)}
          aria-label={`Page ${page + 1}`}
          aria-current={page === currentPage ? 'page' : undefined}
        >
          {page + 1}
        </S.PageButton>
      ))}

      <S.PageButton
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage >= totalPages - 1}
        aria-label="Next page"
      >
        &gt;
      </S.PageButton>
    </S.PaginationContainer>
  );
}
