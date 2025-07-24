export interface Profile {
  nickname: string;
}

export interface ProfileResponse {
  status: number;
  data: Profile;
}
