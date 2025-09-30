import { api } from '@/app/lib/api';

export const patchNotifications = async (notificationId: number): Promise<void> => {
  await api.patch(`/notifications/${notificationId}/read`);
};

export const patchAllNotifications = async (notificationIds: number[]): Promise<void> => {
  await api.patch('/notifications/read-all', { notificationIds });
};
