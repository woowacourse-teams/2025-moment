export interface MomentsResponse {
  status: number;
  data: MyMoments[];
}

export interface MyMoments {
  content: string;
  createdAt: string;
  comment: Comment | null;
}

export interface Comment {
  content: string;
  createdAt: string;
  emoji: Emoji[];
}

export interface Emoji {
  id: number;
  emojiType: string;
  userName: string;
}
