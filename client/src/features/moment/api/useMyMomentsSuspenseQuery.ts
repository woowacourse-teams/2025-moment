import { api } from '@/app/lib/api';
import { queryKeys } from '@/shared/lib/queryKeys';
import type { MomentsResponse } from '../types/moments';
import { useSuspenseInfiniteQuery } from '@tanstack/react-query';

interface GetMyMoments {
  groupId: number | string;
  pageParam?: string | number | null;
}

export const useMyMomentsSuspenseQuery = (groupId: number | string) => {
  const numericGroupId = Number(groupId);
  return useSuspenseInfiniteQuery({
    queryKey: queryKeys.group.myMoments(numericGroupId),
    queryFn: ({ pageParam }: { pageParam: string | number | null }) =>
      getMyMoments({ groupId: numericGroupId, pageParam }),
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};

const getMyMoments = async ({
  groupId,
  pageParam = null,
}: GetMyMoments): Promise<MomentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', String(pageParam));
  }
  params.append('pageSize', '10');

  const response = await api.get(`/groups/${groupId}/my-moments?${params.toString()}`);
  return response.data;
};
