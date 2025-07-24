export interface MomentsResponse {
  status: number;
  data: MyMoments[];
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
}

export interface Comment {
  id: number;
  content: string;
  createdAt: string;
  emoji: Emoji[];
}

export interface Emoji {
  id: number;
  emojiType: string;
  userName: string;
}
