import { useQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import {
  GetRewardHistoryParams,
  RewardHistoryData,
  RewardHistoryResponse,
  UseRewardHistoryQueryOptions,
} from '../types/rewardHistory';

export const useRewardHistoryQuery = ({
  pageNum = 0,
  pageSize = 10,
}: UseRewardHistoryQueryOptions = {}) => {
  return useQuery({
    queryKey: ['rewardHistory', pageNum, pageSize],
    queryFn: () => getRewardHistory({ pageNum, pageSize }),
  });
};

export const getRewardHistory = async ({
  pageNum = 0,
  pageSize = 10,
}: GetRewardHistoryParams = {}): Promise<RewardHistoryData> => {
  const response = await api.get<RewardHistoryResponse>(
    `/me/reward/history?pageNum=${pageNum}&pageSize=${pageSize}`,
  );
  return response.data.data;
};
