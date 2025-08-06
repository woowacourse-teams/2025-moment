import { api } from '@/app/lib/api';
import { NotificationResponse } from '../types/notifications';

export const getNotifications = async (): Promise<NotificationResponse[]> => {
  const response = await api.get('/notifications?read=false');
  return response.data;
};
