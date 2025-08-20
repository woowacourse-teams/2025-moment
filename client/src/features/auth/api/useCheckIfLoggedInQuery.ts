import { useQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';

interface CheckIfLogginedResponse {
  status: number;
  data: {
    isLogged: boolean;
  };
}
export const useCheckIfLoggedInQuery = () => {
  return useQuery({
    queryKey: ['checkIfLoggedIn'],
    queryFn: checkIfLoggined,
    staleTime: 1000 * 60 * 10,
  });
};

const checkIfLoggined = async (): Promise<boolean> => {
  const response = await api.get<CheckIfLogginedResponse>('/auth/login/check');
  return response.data.data.isLogged;
};
