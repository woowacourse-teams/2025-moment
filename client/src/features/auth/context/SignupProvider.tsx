import { SignupContextType, SignupData } from '@/features/auth/types/signup';
import { createContext, ReactNode, useCallback, useMemo, useState } from 'react';

export const SignupContext = createContext<SignupContextType | null>(null);

export const SignupProvider = ({ children }: { children: ReactNode }) => {
  const [signupData, setSignupData] = useState<SignupData>({
    email: '',
    password: '',
    rePassword: '',
    nickname: '',
  });

  const changeSignupData = useCallback((key: keyof SignupData, value: string) => {
    setSignupData(prev => ({ ...prev, [key]: value }));
  }, []);

  const resetSignupData = useCallback(() => {
    setSignupData({
      email: '',
      password: '',
      rePassword: '',
      nickname: '',
    });
  }, []);

  const value = useMemo(
    () => ({ signupData, changeSignupData, resetSignupData }),
    [signupData, changeSignupData, resetSignupData],
  );

  return <SignupContext.Provider value={value}>{children}</SignupContext.Provider>;
};
