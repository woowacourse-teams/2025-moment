import { createContext, useState, useCallback, useEffect, type ReactNode } from 'react';

export type AdminRole = 'ADMIN' | 'VIEWER';

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

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<AdminUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check for existing session on mount
    const storedUser = localStorage.getItem('admin_user');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('admin_user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    // TODO: Replace with actual API call
    setIsLoading(true);
    try {
      // Mock login - replace with actual API call
      const mockUser: AdminUser = {
        id: '1',
        email,
        role: password === 'admin' ? 'ADMIN' : 'VIEWER',
      };
      setUser(mockUser);
      localStorage.setItem('admin_user', JSON.stringify(mockUser));
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    localStorage.removeItem('admin_user');
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
