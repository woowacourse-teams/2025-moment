import { useQuery } from '@tanstack/react-query';
import { getCommentCreationStatus } from '../api/getCommentCreationStatus';

export const useCommentCreationStatusQuery = () => {
  return useQuery({
    queryKey: ['commentCreationStatus'],
    queryFn: getCommentCreationStatus,
  });
};
