import { api } from '@/app/lib/api';

export const logoutUser = async (): Promise<void> => {
  const response = await api.post('/auth/logout');
  return response.data;
};
