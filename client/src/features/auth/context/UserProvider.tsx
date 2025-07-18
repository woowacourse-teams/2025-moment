import { UserContextType, UserData, UserError } from '@/features/auth/types/user';
import { createContext, ReactNode, useMemo, useState } from 'react';

export const UserContext = createContext<UserContextType | null>(null);

export const UserProvider = ({ children }: { children: ReactNode }) => {
  const [userData, setUserData] = useState<UserData>({
    email: '',
    nickname: '',
  });
  const [error, setError] = useState<UserError>({
    emailError: '',
    nicknameError: '',
  });

  const changeUserData = (key: keyof UserData, value: string) => {
    setUserData(prev => ({ ...prev, [key]: value }));
  };

  const resetUserData = () => {
    setUserData({
      email: '',
      nickname: '',
    });
  };

  const value = useMemo(
    () => ({ userData, changeUserData, resetUserData, error }),
    [userData, changeUserData, resetUserData, error],
  );

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>;
};
