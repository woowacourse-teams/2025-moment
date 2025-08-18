export interface ChangeNicknameResponse {
  status: number;
  data: {
    changedNickname: string;
  };
}

export interface ChangeNicknameRequest {
  newNickname: string;
}
