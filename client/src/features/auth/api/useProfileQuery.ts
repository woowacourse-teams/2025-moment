import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';
import { Profile, ProfileResponse } from '../types/profile';
import { AxiosError } from 'axios';

interface UseProfileQueryOptions {
  enabled: boolean;
}

export const useProfileQuery = ({ enabled }: UseProfileQueryOptions) => {
  return useQuery({
    queryKey: ['profile'],
    enabled,
    queryFn: getProfile,
    retry: (failureCount, error) => {
      const axiosError = error as AxiosError;
      if (axiosError?.response?.status === 401 || axiosError?.response?.status === 403) {
        return false;
      }
      return failureCount < 3;
    },
  });
};

export const getProfile = async (): Promise<Profile> => {
  const response = await api.get<ProfileResponse>('/users/me');
  return response.data.data;
};
