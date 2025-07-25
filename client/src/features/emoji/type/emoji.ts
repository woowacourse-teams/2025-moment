export interface EmojiRequest {
  emojiType: string;
  commentId: number;
}

export interface EmojiResponse {
  status: number;
  data: Emoji[];
}

export interface Emoji {
  id: number;
  emojiType: string;
  userName: string;
}
