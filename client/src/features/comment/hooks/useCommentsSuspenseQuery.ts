import { useSuspenseInfiniteQuery } from '@tanstack/react-query';
import { getComments } from '../api/getComments';

/**
 * Suspense를 사용하는 comments query hook
 * ErrorBoundary와 Suspense로 감싸진 컴포넌트에서 사용
 */
export const useCommentsSuspenseQuery = () => {
  return useSuspenseInfiniteQuery({
    queryKey: ['comments'],
    queryFn: getComments,
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};
