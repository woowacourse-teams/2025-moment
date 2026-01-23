import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';
import { InviteInfoResponse } from '../types/group';

export const useInviteInfoQuery = (code: string) => {
  return useQuery({
    queryKey: ['invite', code],
    queryFn: async (): Promise<InviteInfoResponse> => {
      const response = await api.get(`/v2/invite/${code}`);
      return response.data;
    },
    enabled: !!code,
  });
};
