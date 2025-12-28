import { useSuspenseInfiniteQuery } from '@tanstack/react-query';
<<<<<<<< HEAD:client/src/features/comment/hooks/useCommentsSuspenseQuery.ts
import { getComments } from '../api/getComments';
========
import { getComments } from './getComments';
>>>>>>>> 03f6b0e106125c90d2113ffc22fb18550b309f0e:client/src/features/comment/api/useCommentsSuspenseQuery.ts

export const useCommentsSuspenseQuery = () => {
  return useSuspenseInfiniteQuery({
    queryKey: ['comments'],
    queryFn: getComments,
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};
