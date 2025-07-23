export interface EmojiRequest {
  emojiType: string;
  commentId: number;
}

export interface EmojiResponse {
  status: number;
  data: string;
}
