import { useSuspenseInfiniteQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import { MomentsResponse } from '../types/moments';

export const useUnreadMomentsSuspenseQuery = (groupId: number | string) => {
  const numericGroupId = Number(groupId);
  return useSuspenseInfiniteQuery({
    queryKey: ['group', numericGroupId, 'moments', 'unread'],
    queryFn: ({ pageParam }: { pageParam: string | number | null }) =>
      getUnreadMoments({ groupId: numericGroupId, pageParam }),
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};

interface GetMoments {
  groupId: number | string;
  pageParam?: string | number | null;
}

const getUnreadMoments = async ({ groupId, pageParam }: GetMoments): Promise<MomentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', String(pageParam));
  }
  params.append('pageSize', '10');

  const response = await api.get(`/groups/${groupId}/my-moments/unread?${params.toString()}`);
  return response.data;
};
