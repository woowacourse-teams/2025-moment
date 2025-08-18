import { useQuery } from '@tanstack/react-query';
import { getProfile } from '../api/getProfile';

export const useProfileQuery = () => {
  return useQuery({
    queryKey: ['my', 'profile'],
    queryFn: getProfile,
  });
};
