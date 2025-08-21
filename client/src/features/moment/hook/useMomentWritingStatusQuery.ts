import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useQuery } from '@tanstack/react-query';
import { getMomentWritingStatus } from '../api/getMomentWritingStatus';

export const useMomentWritingStatusQuery = () => {
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  return useQuery({
    queryKey: ['momentWritingStatus'],
    queryFn: getMomentWritingStatus,
    enabled: isLoggedIn ?? false,
  });
};
