import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';

export const useRandomNicknameQuery = () => {
  return useQuery<string>({
    queryKey: ['randomNickname'],
    queryFn: getRandomNickname,
  });
};

const getRandomNickname = async (): Promise<string> => {
  const response = await api.get('/users/signup/nickname');
  return response.data.data.randomNickname;
};
