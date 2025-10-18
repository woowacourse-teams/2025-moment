export interface CommentsResponse {
  status: number;
  data: {
    items: CommentItem[];
    nextCursor: string | null;
    hasNextPage: boolean;
    pageSize: number;
  };
}

export interface CommentItem {
  id: number;
  content: string;
  createdAt: string;
  imageUrl?: string | null;
  moment: {
    content: string;
    createdAt: string;
    id: number;
    level: string;
    nickName: string;
    imageUrl?: string | null;
    tagNames: string[];
  } | null;
  echos: Echo[];
  commentNotification: {
    isRead: boolean;
    notificationIds: number[];
  };
}

export interface Echo {
  id: number;
  echoType: string;
  userId: number;
}

export interface SendCommentsData {
  content: string;
  momentId: number;
  imageUrl?: string;
  imageName?: string;
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

export interface GetCommentableMomentsResponse {
  status: number;
  data: GetCommentableMoments;
}

export interface GetCommentableMoments {
  id: number;
  nickname: string;
  level: string;
  content: string;
  imageUrl?: string | null;
  createdAt: string;
}
export interface GetComments {
  pageParam?: string | null;
}

export type FilterType = 'all' | 'unread';
