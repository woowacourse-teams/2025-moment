import { useQuery } from '@tanstack/react-query';
import { getProfile } from '../api/getProfile';

interface UseProfileQueryOptions {
  enabled: boolean;
}

export const useProfileQuery = ({ enabled }: UseProfileQueryOptions) => {
  return useQuery({
    queryKey: ['profile'],
    enabled,
    queryFn: getProfile,
    retry: false, // interceptor 처리
  });
};
