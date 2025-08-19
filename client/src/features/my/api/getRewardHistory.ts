import { api } from '@/app/lib/api';
import { RewardHistoryData, RewardHistoryResponse } from '../types/rewardHistory';

interface GetRewardHistoryParams {
  pageNum?: number;
  pageSize?: number;
}
export const getRewardHistory = async ({
  pageNum = 0,
  pageSize = 10,
}: GetRewardHistoryParams = {}): Promise<RewardHistoryData> => {
  const response = await api.get<RewardHistoryResponse>(
    `/me/reward/history?pageNum=${pageNum}&pageSize=${pageSize}`,
  );
  return response.data.data;
};
