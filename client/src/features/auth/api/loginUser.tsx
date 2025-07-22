import { api } from '@/app/lib/api';
import { LoginFormData, LoginResponse } from '../types/login';

export const loginUser = async (loginData: LoginFormData): Promise<LoginResponse> => {
  const response = await api.post('/auth/login', loginData);
  return response.data;
};
