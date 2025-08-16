export interface EmojiRequest {
  emojiType: string;
  commentId: number;
}

export interface EmojiResponse {
  status: number;
  data: Echos[];
}

export interface Echos {
  id: number;
  echoType: string;
}
