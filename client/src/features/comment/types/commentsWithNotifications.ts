import { MyComments } from '@/features/comment/types/comments';

export interface CommentWithNotifications extends MyComments {
  notificationId: number | null;
  read: boolean;
}
