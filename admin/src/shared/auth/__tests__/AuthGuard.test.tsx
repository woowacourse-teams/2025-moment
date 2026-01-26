import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AuthGuard } from '../AuthGuard';
import { AuthContext, type AuthContextValue, type AdminUser } from '../AuthProvider';

const mockUser: AdminUser = {
  id: '1',
  email: 'admin@test.com',
  role: 'ADMIN',
};

const mockViewerUser: AdminUser = {
  id: '2',
  email: 'viewer@test.com',
  role: 'VIEWER',
};

const renderWithAuth = (
  authValue: AuthContextValue,
  initialPath: string = '/protected'
) => {
  return render(
    <AuthContext.Provider value={authValue}>
      <MemoryRouter initialEntries={[initialPath]}>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route path="/dashboard" element={<div>Dashboard</div>} />
          <Route
            path="/protected"
            element={
              <AuthGuard>
                <div>Protected Content</div>
              </AuthGuard>
            }
          />
          <Route
            path="/admin-only"
            element={
              <AuthGuard requiredRole="ADMIN">
                <div>Admin Only Content</div>
              </AuthGuard>
            }
          />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>
  );
};

describe('AuthGuard', () => {
  it('shows loading state while checking auth', () => {
    const authValue: AuthContextValue = {
      user: null,
      isAuthenticated: false,
      isLoading: true,
      login: vi.fn(),
      logout: vi.fn(),
    };

    renderWithAuth(authValue);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('redirects to login when not authenticated', () => {
    const authValue: AuthContextValue = {
      user: null,
      isAuthenticated: false,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    };

    renderWithAuth(authValue);
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('renders children when authenticated', () => {
    const authValue: AuthContextValue = {
      user: mockUser,
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    };

    renderWithAuth(authValue);
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('redirects VIEWER to dashboard when ADMIN role is required', () => {
    const authValue: AuthContextValue = {
      user: mockViewerUser,
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    };

    renderWithAuth(authValue, '/admin-only');
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
  });

  it('allows ADMIN to access admin-only routes', () => {
    const authValue: AuthContextValue = {
      user: mockUser,
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    };

    renderWithAuth(authValue, '/admin-only');
    expect(screen.getByText('Admin Only Content')).toBeInTheDocument();
  });
});
