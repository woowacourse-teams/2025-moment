import { UserContext } from '@/features/auth/context/UserProvider';
import { useContext } from 'react';

export const useUserContext = () => {
  const userContext = useContext(UserContext);
  if (!userContext) {
    throw new Error('useUserContext must be used within a UserContextProvider');
  }
  return userContext;
};
