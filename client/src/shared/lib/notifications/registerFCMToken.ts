import { api } from '@/app/lib/api';

export const registerFCMToken = async (registrationToken: string) => {
  localStorage.setItem('deviceEndpoint', registrationToken);
  return await api.post<void>('/push-notifications', { deviceEndpoint: registrationToken });
};
