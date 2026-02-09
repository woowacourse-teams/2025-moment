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
  likeCount: number;
  hasLiked: boolean;
  moment: {
    content: string;
    createdAt: string;
    id: number;
    nickName: string;
    memberNickname?: string;
    imageUrl?: string | null;
    likeCount: number;
    hasLiked: boolean;
  } | null;
  commentNotification: {
    isRead: boolean;
    notificationIds: number[];
  };
}

export interface GetCommentableMoments {
  id: number;
  memberId: number;
  nickname: string;
  content: string;
  imageUrl?: string | null;
  createdAt: string;
  likeCount?: number;
  hasLiked?: boolean;
}

export interface GetComments {
  pageParam?: string | number | null;
}

export type FilterType = 'all' | 'unread';
