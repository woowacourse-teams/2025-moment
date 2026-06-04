import { NotificationType } from './notifications';

export interface SSENotification {
  notificationId: number;
  notificationType: NotificationType;
  message: string;
  link: string | null;
}
