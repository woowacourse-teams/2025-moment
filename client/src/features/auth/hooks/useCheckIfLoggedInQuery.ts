import { checkIfLoggined } from '@/features/auth/api/checkIfLoggedIn';
import { useQuery } from '@tanstack/react-query';

export const useCheckIfLoggedInQuery = () => {
  return useQuery({
    queryKey: ['checkIfLoggedIn'],
    queryFn: checkIfLoggined,
    staleTime: 1000 * 60 * 10,
    retry: false,
    refetchOnWindowFocus: false,
    refetchOnMount: false,
  });
};
