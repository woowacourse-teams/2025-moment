import { api } from '@/app/lib/api';

export const registerFCMToken = async (registrationToken: string) => {
  return await api.post<void>('/push-notifications', { deviceEndpoint: registrationToken });
};
