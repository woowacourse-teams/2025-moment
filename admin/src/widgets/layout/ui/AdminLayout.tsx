import { Outlet } from 'react-router-dom';
import { useAuth } from '@shared/auth/useAuth';
import {
  LayoutWrapper,
  Sidebar,
  Logo,
  Nav,
  SidebarNavLink,
  SidebarFooter,
  UserInfoBlock,
  UserEmail,
  UserRole,
  LogoutButton,
  Content,
} from './AdminLayout.styles';

export function AdminLayout() {
  const { user, logout } = useAuth();

  return (
    <LayoutWrapper>
      <Sidebar>
        <Logo>Moment Admin</Logo>
        <Nav>
          <SidebarNavLink to="/dashboard">Dashboard</SidebarNavLink>
          <SidebarNavLink to="/users">Users</SidebarNavLink>
          <SidebarNavLink to="/groups">Groups</SidebarNavLink>
        </Nav>
        <SidebarFooter>
          <UserInfoBlock>
            <UserEmail>{user?.email}</UserEmail>
            <UserRole>{user?.role}</UserRole>
          </UserInfoBlock>
          <LogoutButton onClick={logout}>Logout</LogoutButton>
        </SidebarFooter>
      </Sidebar>
      <Content>
        <Outlet />
      </Content>
    </LayoutWrapper>
  );
}
