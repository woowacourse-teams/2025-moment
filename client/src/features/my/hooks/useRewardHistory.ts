import { useQuery } from '@tanstack/react-query';
import { getRewardHistory } from '../api/getRewardHistory';

interface UseRewardHistoryQueryOptions {
  pageNum?: number;
  pageSize?: number;
  enabled?: boolean;
}

export const useRewardHistoryQuery = ({
  pageNum = 1,
  pageSize = 10,
}: UseRewardHistoryQueryOptions = {}) => {
  return useQuery({
    queryKey: ['rewardHistory', pageNum, pageSize],
    queryFn: () => getRewardHistory({ pageNum, pageSize }),
  });
};
