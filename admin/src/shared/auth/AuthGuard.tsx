import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./useAuth";
import type { ReactNode } from "react";
import type { AdminRole } from "./AuthContext";

interface AuthGuardProps {
  children: ReactNode;
  requiredRole?: AdminRole;
}

export function AuthGuard({ children, requiredRole }: AuthGuardProps) {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    // VIEWER trying to access ADMIN-only route
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
}
