import { api } from '@/app/lib/api';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useQuery } from '@tanstack/react-query';
import { Profile, ProfileResponse } from '../types/profile';

interface UseProfileQueryOptions {
  enabled: boolean;
}

export const useProfileQuery = ({ enabled }: UseProfileQueryOptions) => {
  return useQuery({
    queryKey: queryKeys.auth.profile,
    enabled,
    queryFn: getProfile,
  });
};

export const getProfile = async (): Promise<Profile> => {
  const response = await api.get<ProfileResponse>('/users/me');
  return response.data.data;
};
