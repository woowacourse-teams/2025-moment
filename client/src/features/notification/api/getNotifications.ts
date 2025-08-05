import { api } from '@/app/lib/api';
import { Notification } from '../types/notifications';

export const getNotifications = async (): Promise<Notification[]> => {
  const response = await api.get('/notifications?read=false');
  return response.data;
};
