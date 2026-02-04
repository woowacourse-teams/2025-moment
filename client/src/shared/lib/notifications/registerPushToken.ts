import { api } from '@/app/lib/api';

export const registerPushToken = async (token: string) => {
  localStorage.setItem('deviceEndpoint', token);
  return await api.post<void>('/push-notifications', { deviceEndpoint: token });
};
