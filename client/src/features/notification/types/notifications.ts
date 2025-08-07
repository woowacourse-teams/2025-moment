export type NotificationType = 'NEW_REPLY_ON_COMMENT' | 'NEW_COMMENT_ON_MOMENT';
export type TargetType = 'COMMENT' | 'MOMENT';

export interface NotificationResponse {
  status: number;
  data: NotificationItem[];
}

export interface NotificationItem {
  id?: number;
  notificationType: NotificationType;
  targetType: TargetType;
  targetId: number;
  message: string;
  isRead: boolean;
}
