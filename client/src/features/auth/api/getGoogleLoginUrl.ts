import { api } from '@/app/lib/api';
import { GoogleLoginUrlResponse } from '../types/login';

export const getGoogleLoginUrl = async (): Promise<GoogleLoginUrlResponse> => {
  const response = await api.get<GoogleLoginUrlResponse>('/auth/google/login');
  return response.data;
};
