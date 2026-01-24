import { api } from '@/app/lib/api';
import type { MomentsResponse } from '../types/moments';
import { useSuspenseInfiniteQuery } from '@tanstack/react-query';

interface GetGroupFeed {
  groupId: number | string;
  pageParam?: string | number | null;
}

export const useGroupFeedQuery = (groupId: number | string) => {
  return useSuspenseInfiniteQuery({
    queryKey: ['group', groupId, 'moments'],
    queryFn: ({ pageParam }: { pageParam: string | number | null }) =>
      getGroupFeed({ groupId, pageParam }),
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};

const getGroupFeed = async ({
  groupId,
  pageParam = null,
}: GetGroupFeed): Promise<MomentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', String(pageParam));
  }
  params.append('pageSize', '10');

  const response = await api.get(`/groups/${groupId}/moments?${params.toString()}`);
  return response.data;
};
