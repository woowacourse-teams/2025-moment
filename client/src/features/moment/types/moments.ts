import type { Echos } from '@/features/echo/type/echos';

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
  comment: Comment[] | null;
}

interface Comment {
  id: number;
  content: string;
  commenterName: string;
  commenterLevel: string;
  createdAt: string;
  echos: Echos[];
}

export interface MomentWritingStatusResponse {
  data: {
    status: 'DENIED' | 'ALLOWED';
  };
}

export interface MomentExtraWritableResponse {
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
