import { useQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';

export const useProfileQuery = () => {
  return useQuery({
    queryKey: ['my', 'profile'],
    queryFn: getProfile,
  });
};

export const getProfile = async () => {
  const response = await api.get('/me/profile');
  return response.data.data;
};
