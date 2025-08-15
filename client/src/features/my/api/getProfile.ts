import { api } from '@/app/lib/api';

export const getProfile = async () => {
  const response = await api.get('/api/v1/my/profile');
  return response.data;
};
