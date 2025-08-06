import { api } from '@/app/lib/api';
import type { MomentsResponse } from '../types/moments';

interface GetMoments {
  pageParam?: string | null;
}

export const getMoments = async ({ pageParam = null }: GetMoments): Promise<MomentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('cursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get(`/moments/me?${params.toString()}`);
  return response.data;
};
