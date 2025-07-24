import { useQuery } from '@tanstack/react-query';
import { matchMoments } from '../api/matchMoments';

export const useMatchMomentsQuery = () => {
  return useQuery({
    queryKey: ['matchMoments'],
    queryFn: matchMoments,
  });
};
