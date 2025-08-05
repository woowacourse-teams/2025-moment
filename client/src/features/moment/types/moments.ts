export interface MomentsResponse {
  // TODO: 서버측에서 Reponse 필드 바꾼 것 같음. 일단 변경된 값에 맞게 수정 후 추후 물어보기
  status: number;
  data: {
    hasNextPage: boolean;
    items: MyMoments[];
    nextCursor: any;
    pageSize: number;
  };
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
  id: number;
  content: string;
  createdAt: string;
  comment: Comment | null;
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
