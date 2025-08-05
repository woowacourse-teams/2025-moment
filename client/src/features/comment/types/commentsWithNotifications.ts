import { MyComments } from '@/features/comment/types/comments';

export interface CommentWithNotifications extends MyComments {
  read: boolean;
}
