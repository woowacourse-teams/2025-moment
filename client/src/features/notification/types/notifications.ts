export type NotificationType =
  | 'NEW_COMMENT_ON_MOMENT'
  | 'GROUP_JOIN_REQUEST'
  | 'GROUP_JOIN_APPROVED'
  | 'GROUP_KICKED'
  | 'MOMENT_LIKED'
  | 'COMMENT_LIKED';

export interface NotificationResponse {
  status: number;
  data: NotificationItem[];
}

export interface NotificationItem {
  id: number;
  notificationType: NotificationType;
  message: string;
  isRead: boolean;
  link: string | null;
}
