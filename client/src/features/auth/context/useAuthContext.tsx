import { AuthContext } from '@/features/auth/context/AuthProvider';
import { useContext } from 'react';

export const useAuthContext = () => {
  return useContext(AuthContext);
};
