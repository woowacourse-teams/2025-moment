import { queryClient } from '@/app/lib/queryClient';
import { useEffect } from 'react';
import { checkIfLoggined } from '@/features/auth/api/useCheckIfLoggedInQuery';

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  useEffect(() => {
    const checkInitialAuth = async () => {
      try {
        const isLoggedIn = await checkIfLoggined();
        queryClient.setQueryData(['checkIfLoggedIn'], isLoggedIn);
      } catch {
        queryClient.setQueryData(['checkIfLoggedIn'], false);
      }
    };

    checkInitialAuth();
  }, []);

  return <>{children}</>;
};
