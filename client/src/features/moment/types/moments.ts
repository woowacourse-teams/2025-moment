export interface MomentsResponse {
  status: number;
  data: {
    moments: MyMomentsItem[];
    nextCursor: string | number | null;
    hasNextPage: boolean;
    pageSize: number;
  };
}

export interface MyMomentsItem {
  id: number;
  momentId: number;
  momenterId: number;
  memberId: number;
  content: string;
  memberNickname: string;
  createdAt: string;
  imageUrl?: string | null;
  likeCount?: number;
  hasLiked?: boolean;
  commentCount?: number;
  comments: Comment[] | null;
  momentNotification: {
    isRead: boolean;
    notificationIds: number[];
  };
}

export interface Comment {
  id: number;
  content: string;
  nickname: string;
  memberNickname: string; // V2
  createdAt: string;
  imageUrl?: string | null;
  likeCount?: number;
  hasLiked?: boolean;
}

export type FilterType = 'all' | 'unread';
