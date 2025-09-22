import { useQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import { AxiosError } from 'axios';

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
    retry: (failureCount, error) => {
      const axiosError = error as AxiosError;
      if (axiosError?.response?.status === 401 || axiosError?.response?.status === 403) {
        return false;
      }
      return failureCount < 3;
    },
  });
};

export const checkIfLoggined = async (): Promise<boolean> => {
  const response = await api.get<CheckIfLogginedResponse>('/auth/login/check');
  return response.data.data.isLogged;
};
