import { api } from '@/app/lib/api';
import type { MomentsResponse } from '../types/moments';
import { useSuspenseInfiniteQuery } from '@tanstack/react-query';

interface GetMoments {
  groupId: number | string;
  pageParam?: string | null;
}

export const useMomentsSuspenseQuery = (groupId: number | string) => {
  return useSuspenseInfiniteQuery({
    queryKey: ['group', groupId, 'my-moments'],
    queryFn: ({ pageParam }: { pageParam: string | null }) => getMoments({ groupId, pageParam }),
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};

const getMoments = async ({ groupId, pageParam = null }: GetMoments): Promise<MomentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get(`/groups/${groupId}/my-moments?${params.toString()}`);
  return response.data;
};
