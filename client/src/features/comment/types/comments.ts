import type { Emoji } from '@/features/emoji/type/emoji';

export interface CommentsResponse {
  status: number;
  data: {
    items: CommentItem[];
    nextCursor: string | null;
    hasNextPage: boolean;
    pageSize: number;
  };
}

interface CommentItem {
  id: number;
  content: string;
  createdAt: string;
  moment: {
    content: string;
    createdAt: string;
  };
  emojis: Emoji[];
}

export interface SendCommentsData {
  content: string;
  momentId: number;
}

export interface SendCommentsResponse {
  status: number;
  data: {
    commentId: number;
    content: string;
    createdAt: string;
  };
}

export interface SendCommentsError {
  code: string;
  message: string;
  status: number;
}

export type CommentCreationStatus = 'NOT_MATCHED' | 'ALREADY_COMMENTED' | 'WRITABLE';

export interface CommentCreationStatusResponse {
  status: number;
  data: {
    commentCreationStatus: CommentCreationStatus;
  };
}
