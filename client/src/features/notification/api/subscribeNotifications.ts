import { BASE_URL } from '@/app/lib/api';

export const subscribeNotifications = (): EventSource => {
  return new EventSource(`${BASE_URL}/notifications/subscribe`, { withCredentials: true });
};
