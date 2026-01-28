import styled from "@emotion/styled";
import { useNavigate } from "react-router-dom";
import { UserTable } from "@features/user/ui/UserTable";
import { Pagination } from "@features/user/ui/Pagination";
import { useUserList } from "@features/user/hooks/useUserList";

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
`;

const TotalCount = styled.span`
  font-size: 0.875rem;
  color: #9ca3af;
`;

export default function UserListPage() {
  const navigate = useNavigate();
  const {
    users,
    totalPages,
    totalElements,
    currentPage,
    isLoading,
    isError,
    handlePageChange,
  } = useUserList();

  const handleUserClick = (userId: number) => {
    navigate(`/users/${userId}`);
  };

  return (
    <Container>
      <Header>
        <Title>Users</Title>
        {!isLoading && !isError && (
          <TotalCount>Total: {totalElements}</TotalCount>
        )}
      </Header>

      <UserTable
        users={users}
        isLoading={isLoading}
        isError={isError}
        onUserClick={handleUserClick}
      />
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={handlePageChange}
      />
    </Container>
  );
}
