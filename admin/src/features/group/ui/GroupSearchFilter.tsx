import type { GroupStatus } from "../types/group";
import * as S from "./GroupSearchFilter.styles";

interface GroupSearchFilterProps {
  searchInput: string;
  onSearchInputChange: (value: string) => void;
  onSearch: () => void;
  status: GroupStatus | "";
  onStatusChange: (status: GroupStatus | "") => void;
}

export function GroupSearchFilter({
  searchInput,
  onSearchInputChange,
  onSearch,
  status,
  onStatusChange,
}: GroupSearchFilterProps) {
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      onSearch();
    }
  };

  return (
    <S.FilterContainer>
      <S.SearchInput
        type="text"
        placeholder="Search by group name..."
        value={searchInput}
        onChange={(e) => onSearchInputChange(e.target.value)}
        onKeyDown={handleKeyDown}
      />
      <S.Select
        value={status}
        onChange={(e) => onStatusChange(e.target.value as GroupStatus | "")}
      >
        <option value="">All Status</option>
        <option value="ACTIVE">Active</option>
        <option value="DELETED">Deleted</option>
      </S.Select>
      <S.SearchButton onClick={onSearch}>Search</S.SearchButton>
    </S.FilterContainer>
  );
}
