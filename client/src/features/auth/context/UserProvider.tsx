import { UserContextType, UserData, UserError } from '@/features/auth/types/signup';
import { createContext, ReactNode, useCallback, useMemo, useState } from 'react';

export const UserContext = createContext<UserContextType | null>(null);

export const UserProvider = ({ children }: { children: ReactNode }) => {
  const [signupData, setUserData] = useState<UserData>({
    email: '',
    nickname: '',
  });
  const [error, setError] = useState<UserError>({
    emailError: '',
    nicknameError: '',
  });

  const changeUserData = useCallback((key: keyof UserData, value: string) => {
    setUserData(prev => ({ ...prev, [key]: value }));
  }, []);

  const resetUserData = useCallback(() => {
    setUserData({
      email: '',
      nickname: '',
    });
  }, []);

  const value = useMemo(
    () => ({ signupData, changeUserData, resetUserData, error }),
    [signupData, changeUserData, resetUserData, error],
  );

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>;
};
