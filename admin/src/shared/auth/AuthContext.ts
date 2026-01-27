import { createContext } from "react";

export type AdminRole = "ADMIN" | "VIEWER";

export interface AdminUser {
  id: string;
  email: string;
  role: AdminRole;
}

export interface AuthContextValue {
  user: AdminUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
