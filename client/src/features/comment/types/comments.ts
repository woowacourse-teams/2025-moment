export interface CommentsResponse {
  status: number;
  data: {
    comments: CommentItem[];
    nextCursor: string | number | null;
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
    memberNickname?: string;
    imageUrl?: string | null;
  } | null;
  commentNotification: {
    isRead: boolean;
    notificationIds: number[];
  };
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
  pageParam?: string | number | null;
}

export type FilterType = 'all' | 'unread';
