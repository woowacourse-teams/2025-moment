import { NotificationType, TargetType } from './notifications';

export interface SSENotification {
  notificationId?: number;
  notificationType: NotificationType;
  targetType: TargetType;
  targetId: number;
  groupId: number;
  message: string;
  isRead: boolean;
  link: string;
}
