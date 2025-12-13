import { api } from '@/app/lib/api';
import { MomentExtraWritableResponse } from '../types/moments';
import { useQuery } from '@tanstack/react-query';

export const useMomentExtraWritableQuery = () => {
  return useQuery({
    queryKey: ['momentExtraWritable'],
    queryFn: getMomentExtraWritable,
  });
};

const getMomentExtraWritable = async (): Promise<MomentExtraWritableResponse> => {
  const response = await api.get('/moments/writable/extra');
  return response.data;
};
