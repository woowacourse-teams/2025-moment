import { api } from '@/app/lib/api';

export const checkNicknameExist = async (nickname: string) => {
  const response = await api.post('/users/signup/nickname/check', { nickname });
  return response.data;
};
