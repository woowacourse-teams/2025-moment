import { useQuery } from '@tanstack/react-query';
import { getProfile } from '../api/getProfile';

export const useProfileQuery = () => {
  return useQuery({
    queryKey: ['profile'],
    queryFn: getProfile,
    retry: false, // interceptor 처리
  });
};
