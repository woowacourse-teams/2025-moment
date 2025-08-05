export type NotificationType = 'COMMENT_REPLY' | 'MOMENT_COMMENT';
export type TargetType = 'COMMENT' | 'MOMENT';

export interface Notification {
  id: string;
  notification_type: NotificationType;
  target_type: TargetType;
  target_id: string;
  message: string;
}
