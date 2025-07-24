import { useQuery } from '@tanstack/react-query';
import { getMoments } from '../api/getMoments';

export const useMomentsQuery = () => {
  return useQuery({
    queryKey: ['moments'],
    queryFn: getMoments,
  });
};
