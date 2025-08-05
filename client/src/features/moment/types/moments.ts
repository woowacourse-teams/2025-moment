export interface MomentsResponse {
  status: number;
  data: MyMoments[];
}

export interface CheckMomentsResponse {
  data: {
    status: 'DENIED' | 'ALLOWED';
  };
}

export interface MatchMomentsResponse {
  status: number;
  data: {
    id: number;
    content: string;
    createdAt: string;
  };
}

export interface MyMoments {
  content: string;
  createdAt: string;
  comment: Comment | null;
  nextCursor: string | null;
  hasNextPage: boolean;
  pageSize: number;
}

export interface Comment {
  id: number;
  content: string;
  createdAt: string;
  emojis: Emoji[];
}

export interface Emoji {
  id: number;
  emojiType: string;
  userName: string;
}
