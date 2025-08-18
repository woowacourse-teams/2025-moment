import { useQuery } from '@tanstack/react-query';
import { getMomentWritingStatus } from '../api/getMomentWritingStatus';

export const useMomentWritingStatusQuery = () => {
  return useQuery({
    queryKey: ['momentWritingStatus'],
    queryFn: getMomentWritingStatus,
  });
};
