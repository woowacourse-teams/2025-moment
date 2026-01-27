import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthGuard } from '@shared/auth/AuthGuard';
import { AdminLayout } from '@widgets/layout';
import LoginPage from '@pages/LoginPage';
import DashboardPage from '@pages/DashboardPage';
import UserListPage from '@pages/UserListPage';
import UserDetailPage from '@pages/UserDetailPage';
import GroupListPage from '@pages/GroupListPage';
import GroupDetailPage from '@pages/GroupDetailPage';

export function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <AuthGuard>
            <AdminLayout />
          </AuthGuard>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/users" element={<UserListPage />} />
        <Route path="/users/:id" element={<UserDetailPage />} />
        <Route path="/groups" element={<GroupListPage />} />
        <Route path="/groups/:id" element={<GroupDetailPage />} />
      </Route>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
