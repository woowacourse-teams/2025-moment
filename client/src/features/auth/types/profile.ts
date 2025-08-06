export interface Profile {
  nickname: string;
  level: string;
}

export interface ProfileResponse {
  status: number;
  data: Profile;
}
