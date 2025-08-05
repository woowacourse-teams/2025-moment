import { api } from '@/app/lib/api';

export const patchNotifications = async (notificationId: number) => {
  const response = await api.patch(`/notifications/${notificationId}/read`);
  return response.data;
};
