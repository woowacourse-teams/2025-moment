import { api } from '@/app/lib/api';
import { ProfileResponse } from '../types/profile';

export const getProfile = async (): Promise<ProfileResponse> => {
  const response = await api.get<ProfileResponse>('/users/me');
  return response.data;
};
