import { useState } from 'react';
import { useUsersQuery } from '../api/useUsersQuery';

const DEFAULT_PAGE_SIZE = 20;

export function useUserList() {
  const [page, setPage] = useState(0);
  const [size] = useState(DEFAULT_PAGE_SIZE);

  const { data, isLoading, isError } = useUsersQuery({ page, size });

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  return {
    users: data?.content ?? [],
    totalPages: data?.totalPages ?? 0,
    totalElements: data?.totalElements ?? 0,
    currentPage: page,
    isLoading,
    isError,
    handlePageChange,
  };
}
