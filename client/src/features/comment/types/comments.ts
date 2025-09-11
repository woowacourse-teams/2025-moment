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
  moment: {
    content: string;
    createdAt: string;
    id: number;
    level: string;
    nickName: string;
    tagNames: string[];
  };
  echos: Echo[];
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
  createdAt: string;
}

export type FilterType = 'all' | 'unread';

export interface UnreadCommentItem extends Omit<CommentItem, 'moment'> {
  moment: CommentItem['moment'] & {
    tagNames: string;
  };
}

export interface UnreadCommentsResponse {
  status: number;
  data: {
    items: UnreadCommentItem[];
    nextCursor: string | null;
    hasNextPage: boolean;
    pageSize: number;
  };
}

export interface GetUnreadComments {
  pageParam?: string | null;
}
