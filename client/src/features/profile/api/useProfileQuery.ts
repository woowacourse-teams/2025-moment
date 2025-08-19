import { api } from '@/app/lib/api';
import { Profile, ProfileResponse } from '@/features/profile/types/profile';
import { useQuery } from '@tanstack/react-query';

interface UseProfileQueryOptions {
  enabled: boolean;
}

export const useProfileQuery = ({ enabled }: UseProfileQueryOptions) => {
  return useQuery({
    queryKey: ['profile'],
    enabled,
    queryFn: getProfile,
    retry: false, // interceptor 처리
  });
};

export const getProfile = async (): Promise<Profile> => {
  const response = await api.get<ProfileResponse>('/me/profile');
  return response.data.data;
};
