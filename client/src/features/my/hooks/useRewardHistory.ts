import { useQuery } from '@tanstack/react-query';
import { getRewardHistory } from '../api/getRewardHistory';

export const useRewardHistory = () => {
  return useQuery({
    queryKey: ['my', 'reward', 'history'],
    queryFn: getRewardHistory,
  });
};
