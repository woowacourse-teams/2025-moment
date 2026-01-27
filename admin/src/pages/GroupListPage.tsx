import styled from "@emotion/styled";
import { useNavigate } from "react-router-dom";
import { GroupTable } from "@features/group/ui/GroupTable";
import { GroupSearchFilter } from "@features/group/ui/GroupSearchFilter";
import { Pagination } from "@features/user/ui/Pagination";
import { useGroupList } from "@features/group/hooks/useGroupList";

const Container = styled.div`
  padding: 2rem;
`;

const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
`;

const Title = styled.h1`
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
`;

const TotalCount = styled.span`
  font-size: 0.875rem;
  color: #6b7280;
`;

export default function GroupListPage() {
  const navigate = useNavigate();
  const {
    groups,
    totalPages,
    totalElements,
    currentPage,
    isLoading,
    isError,
    searchInput,
    setSearchInput,
    status,
    handleSearch,
    handleStatusChange,
    handlePageChange,
  } = useGroupList();

  const handleGroupClick = (groupId: number) => {
    navigate(`/groups/${groupId}`);
  };

  return (
    <Container>
      <Header>
        <Title>Groups</Title>
        {!isLoading && !isError && (
          <TotalCount>Total: {totalElements}</TotalCount>
        )}
      </Header>

      <GroupSearchFilter
        searchInput={searchInput}
        onSearchInputChange={setSearchInput}
        onSearch={handleSearch}
        status={status}
        onStatusChange={handleStatusChange}
      />

      <GroupTable
        groups={groups}
        isLoading={isLoading}
        isError={isError}
        onGroupClick={handleGroupClick}
      />
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={handlePageChange}
      />
    </Container>
  );
}
