import { api } from '@/app/lib/api';
import { MomentWritingStatusResponse } from '../types/moments';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useQuery } from '@tanstack/react-query';

export const useMomentWritingStatusQuery = () => {
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  return useQuery({
    queryKey: ['momentWritingStatus'],
    queryFn: getMomentWritingStatus,
    enabled: isLoggedIn ?? false,
  });
};

const getMomentWritingStatus = async (): Promise<MomentWritingStatusResponse> => {
  const response = await api.get('moments/writable/basic');
  return response.data;
};
