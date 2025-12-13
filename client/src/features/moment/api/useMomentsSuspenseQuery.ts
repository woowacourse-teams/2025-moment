import { api } from '@/app/lib/api';
import type { MomentsResponse } from '../types/moments';
import { useSuspenseInfiniteQuery } from '@tanstack/react-query';

interface GetMoments {
  pageParam?: string | null;
}

export const useMomentsSuspenseQuery = () => {
  return useSuspenseInfiniteQuery({
    queryKey: ['moments'],
    queryFn: ({ pageParam }: { pageParam: string | null }) => getMoments({ pageParam }),
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};

const getMoments = async ({ pageParam = null }: GetMoments): Promise<MomentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get(`/moments/me?${params.toString()}`);
  return response.data;
};
