import styled from '@emotion/styled';
import { useAuth } from '@shared/auth/useAuth';

const Container = styled.div`
  padding: 2rem;
`;

const Header = styled.header`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
`;

const Title = styled.h1`
  font-size: 1.5rem;
  font-weight: 600;
`;

const UserInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
`;

const LogoutButton = styled.button`
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  background-color: #ef4444;
  color: white;
  cursor: pointer;

  &:hover {
    background-color: #dc2626;
  }
`;

export default function DashboardPage() {
  const { user, logout } = useAuth();

  return (
    <Container>
      <Header>
        <Title>Moment Admin Dashboard</Title>
        <UserInfo>
          <span>
            {user?.email} ({user?.role})
          </span>
          <LogoutButton onClick={logout}>Logout</LogoutButton>
        </UserInfo>
      </Header>
      <main>
        <p>Welcome to the Moment Admin Panel.</p>
      </main>
    </Container>
  );
}
