import { useQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';

export const useProfileQuery = () => {
  return useQuery({
    queryKey: ['my', 'profile'],
    queryFn: getProfile,
  });
};

const getProfile = async () => {
  const response = await api.get('/me/profile');
  return response.data.data;
};
