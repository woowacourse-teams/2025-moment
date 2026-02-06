import { NotificationType, TargetType } from './notifications';

export interface SSENotification {
  notificationId?: number;
  notificationType: NotificationType;
  message: string;
  link: string;
  targetType?: TargetType;
  targetId?: number;
  isRead?: boolean;
}
