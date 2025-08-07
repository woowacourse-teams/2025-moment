import { getRandomNickname } from '@/features/auth/api/getRandomNickname';
import { useQuery } from '@tanstack/react-query';

export const useRandomNicknameQuery = () => {
  return useQuery<string>({
    queryKey: ['randomNickname'],
    queryFn: getRandomNickname,
  });
};
