import type { Echos } from '@/features/echo/type/echos';

export interface MomentsRequest {
  content: string;
  tagNames: string[];
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
  comments: Comment[] | null;
  tagNames: { tagNames: string[] }; // TODO: 추후 DTO 변경 예정
}

export interface Comment {
  id: number;
  content: string;
  nickname: string;
  level: string;
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
