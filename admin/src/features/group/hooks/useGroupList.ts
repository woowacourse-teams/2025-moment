import { useState } from "react";
import { useGroupsQuery } from "../api/useGroupsQuery";
import type { GroupStatus } from "../types/group";

const DEFAULT_PAGE_SIZE = 20;

export function useGroupList() {
  const [page, setPage] = useState(0);
  const [size] = useState(DEFAULT_PAGE_SIZE);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState<GroupStatus | "">("");
  const [searchInput, setSearchInput] = useState("");

  const { data, isLoading, isError } = useGroupsQuery({
    page,
    size,
    keyword,
    status,
  });

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  const handleSearch = () => {
    setKeyword(searchInput);
    setPage(0);
  };

  const handleStatusChange = (newStatus: GroupStatus | "") => {
    setStatus(newStatus);
    setPage(0);
  };

  return {
    groups: data?.content ?? [],
    totalPages: data?.totalPages ?? 0,
    totalElements: data?.totalElements ?? 0,
    currentPage: page,
    isLoading,
    isError,
    searchInput,
    setSearchInput,
    status,
    handleSearch,
    handleStatusChange,
    handlePageChange,
  };
}
