import { api } from '@/app/lib/api';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useQuery } from '@tanstack/react-query';
import { BlockedUser, BlockListResponse } from '../types/block';

export const useBlockListQuery = () => {
  return useQuery({
    queryKey: queryKeys.blocks.all,
    queryFn: getBlockList,
  });
};

const getBlockList = async (): Promise<BlockedUser[]> => {
  const response = await api.get<BlockListResponse>('/users/blocks');
  return response.data.data;
};
