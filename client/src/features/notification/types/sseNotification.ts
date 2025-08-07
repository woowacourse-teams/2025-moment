import { NotificationType, TargetType } from './notifications';

export interface SSENotification {
  notificationType: NotificationType;
  targetType: TargetType;
  targetId: number;
  message: string;
  isRead: boolean;
}
