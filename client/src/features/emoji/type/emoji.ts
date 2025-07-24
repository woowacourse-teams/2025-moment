// TODO: 스티커 타입 추가되면 더 추가하기
export type EmojiType = 'HEART';

export interface EmojiRequest {
  emojiType: EmojiType;
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
