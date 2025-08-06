export type NotificationType = 'NEW_REPLY_ON_COMMENT' | 'NEW_COMMENT_ON_MOMENT';
export type TargetType = 'COMMENT' | 'MOMENT';

export interface NotificationResponse {
  status: string;
  data: {
    id?: string;
    notification_type: NotificationType;
    target_type: TargetType;
    target_id: number;
    message: string;
  };
}
