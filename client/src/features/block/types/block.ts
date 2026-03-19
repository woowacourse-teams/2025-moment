export interface BlockedUser {
  blockedUserId: number;
  nickname: string;
  createdAt: string;
}

export interface BlockListResponse {
  data: BlockedUser[];
}
