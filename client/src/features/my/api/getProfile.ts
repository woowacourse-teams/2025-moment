import { api } from '@/app/lib/api';

export const getProfile = async () => {
  const response = await api.get('/me/profile');
  return response.data;
};
