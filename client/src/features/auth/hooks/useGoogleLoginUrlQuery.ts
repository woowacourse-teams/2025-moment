import { useQuery } from '@tanstack/react-query';
import { getGoogleLoginUrl } from '../api/getGoogleLoginUrl';

export const useGoogleLoginUrlQuery = () => {
  return useQuery({
    queryKey: ['google-login-url'],
    queryFn: getGoogleLoginUrl,
  });
};
