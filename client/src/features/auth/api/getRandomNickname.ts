import { api } from '@/app/lib/api';

export const getRandomNickname = async (): Promise<string> => {
  const response = await api.get('/users/signup/nickname');
  return response.data.data.randomNickname;
};
