import { api } from '@/app/lib/api';

export const checkEmailExist = async (email: string) => {
  const response = await api.post('/users/signup/email/check', { email });
  return response.data;
};
