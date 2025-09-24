import { useQuery } from '@tanstack/react-query';
import { api, BASE_URL } from '@/app/lib/api';
import axios from 'axios';

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

export const checkIfLoggined = async (): Promise<boolean> => {
  try {
    const response = await api.get<CheckIfLogginedResponse>('/auth/login/check');

    if (!response.data.data.isLogged) {
      try {
        const refreshApi = axios.create({
          baseURL: BASE_URL,
          headers: { 'Content-Type': 'application/json' },
          timeout: 10000,
          withCredentials: true,
        });

        await refreshApi.post('/auth/refresh');

        // refreshToken 성공 후 다시 체크
        const retryResponse = await api.get<CheckIfLogginedResponse>('/auth/login/check');
        return retryResponse.data.data.isLogged;
      } catch (refreshError) {
        // refreshToken 실패 시 원래 결과 반환
        return response.data.data.isLogged;
      }
    }

    return response.data.data.isLogged;
  } catch (error) {
    console.log('❌ checkIfLoggined error:', error);
    throw error;
  }
};
