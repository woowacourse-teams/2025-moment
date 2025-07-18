import { UserContext } from '@/features/auth/context/UserProvider';
import { useContext } from 'react';

export const useUserContext = () => {
  const signupContext = useContext(UserContext);
  if (!signupContext) {
    throw new Error('useUserContext must be used within a UserContextProvider');
  }
  return signupContext;
};
