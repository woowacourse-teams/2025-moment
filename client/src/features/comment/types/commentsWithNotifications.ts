import { CommentItem } from '@/features/comment/types/comments';

export interface CommentWithNotifications extends CommentItem {
  notificationId: number | null;
  read: boolean;
}
