import { useQuery } from '@tanstack/react-query';
import { getComments } from '../api/getComments';

export const useCommentsQuery = () => {
  return useQuery({
    queryKey: ['comments'],
    queryFn: getComments,
  });
};
