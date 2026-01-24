import { api } from '@/app/lib/api';
import { MomentWritingStatusResponse } from '../types/moments';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useQuery } from '@tanstack/react-query';

export const useMomentWritingStatusQuery = (groupId: string | undefined) => {
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  return useQuery({
    queryKey: ['momentWritingStatus', groupId],
    queryFn: () => getMomentWritingStatus(groupId),
    enabled: (isLoggedIn ?? false) && !!groupId,
  });
};

const getMomentWritingStatus = async (
  groupId: string | undefined,
): Promise<MomentWritingStatusResponse> => {
  const response = await api.get(`/groups/${groupId}/moments/writable/basic`);
  return response.data;
};
