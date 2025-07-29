import { useQuery } from '@tanstack/react-query';
import { getCheckMoments } from '../api/getCheckMoments';

export const useCheckMomentsQuery = () => {
  return useQuery({
    queryKey: ['checkMoments'],
    queryFn: getCheckMoments,
  });
};
