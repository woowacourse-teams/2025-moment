import { useQuery } from '@tanstack/react-query';
import { getMomentExtraWritable } from '../api/getMomentExtraWritable';

export const useMomentExtraWritableQuery = () => {
  return useQuery({
    queryKey: ['momentExtraWritable'],
    queryFn: getMomentExtraWritable,
  });
};
