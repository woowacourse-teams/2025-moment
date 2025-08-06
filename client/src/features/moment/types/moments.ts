import type { Emoji } from '@/features/emoji/type/emoji';

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
  comment?: Comment;
}

interface Comment {
  id: number;
  content: string;
  createdAt: string;
  emojis: Emoji[];
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
