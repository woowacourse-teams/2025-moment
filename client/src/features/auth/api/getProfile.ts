import { api } from '@/app/lib/api';
import { Profile, ProfileResponse } from '../types/profile';

export const getProfile = async (): Promise<Profile> => {
  const response = await api.get<ProfileResponse>('/users/me');
  return response.data.data;
};
