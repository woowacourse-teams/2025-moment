export interface MomentsRequest {
  content: string;
}

export interface MomentsResponse {
  status: number;
  data: {
    items: MyMomentsItem[];
    nextCursor: string | null;
    hasNextPage: boolean;
    pageSize: number;
  };
}

export interface MyMomentsItem {
  id: number;
  momenterId: number;
  content: string;
  createdAt: string;
  imageUrl?: string | null;
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
  level: string;
  createdAt: string;
  imageUrl?: string | null;
}

export interface MatchMomentsResponse {
  status: number;
  data: {
    id: number;
    content: string;
    createdAt: string;
  };
}
