import { api } from '@/app/lib/api';

export const getRewardHistory = async () => {
  const response = await api.get('/api/v1/my/reward/history');
  return response.data;
};
