import { SignupContextType, SignupData, SignupError } from '@/features/auth/types/signup';
import { createContext, ReactNode, useCallback, useMemo, useState } from 'react';

export const SignupContext = createContext<SignupContextType | null>(null);

export const SignupProvider = ({ children }: { children: ReactNode }) => {
  const [signupData, setSignupData] = useState<SignupData>({
    email: '',
    password: '',
    rePassword: '',
    nickname: '',
  });
  const [error, setError] = useState<SignupError>({
    emailError: '',
    passwordError: '',
    rePasswordError: '',
    nicknameError: '',
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
    () => ({ signupData, changeSignupData, resetSignupData, error }),
    [signupData, changeSignupData, resetSignupData, error],
  );

  return <SignupContext.Provider value={value}>{children}</SignupContext.Provider>;
};
