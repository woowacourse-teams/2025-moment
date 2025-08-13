import { api } from '@/app/lib/api';

interface CheckIfLogginedResponse {
  status: number;
  data: {
    isLogged: boolean;
  };
}

export const checkIfLoggined = async (): Promise<boolean> => {
  const response = await api.get<CheckIfLogginedResponse>('/auth/login/check');
  return response.data.data.isLogged;
};
