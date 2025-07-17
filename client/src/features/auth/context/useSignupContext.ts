import { SignupContext } from '@/features/auth/context/SignupProvider';
import { useContext } from 'react';

export const useSignupContext = () => {
  const signupContext = useContext(SignupContext);
  if (!signupContext) {
    throw new Error('useSignupContext must be used within a SignupContextProvider');
  }
  return signupContext;
};
