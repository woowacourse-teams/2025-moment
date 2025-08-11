import { createContext, useMemo, useState } from 'react';

interface AuthContextType {
  isLoggedIn: boolean;
  setIsLoggedIn: (value: boolean) => void;
}

export const AuthContext = createContext<AuthContextType>({
  isLoggedIn: false,
  setIsLoggedIn: () => {},
});

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const getInitialLogin = () => {
    return sessionStorage.getItem('isLoggedIn') === 'true';
  };

  const [isLoggedIn, setIsLoggedInState] = useState(getInitialLogin());

  const setIsLoggedIn = (value: boolean) => {
    setIsLoggedInState(value);
    if (value) {
      sessionStorage.setItem('isLoggedIn', 'true');
    } else {
      sessionStorage.removeItem('isLoggedIn');
    }
  };

  const value = useMemo(
    () => ({
      isLoggedIn,
      setIsLoggedIn,
    }),
    [isLoggedIn],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
