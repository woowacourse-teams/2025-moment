import { api } from '@/app/lib/api';

export const checkEmailExist = async (email: string) => {
  const response = await api.post('/users/email/check', email);
  return response.data;
};
