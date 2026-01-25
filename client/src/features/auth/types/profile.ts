export interface Profile {
  id: number;
  nickname: string;
}

export interface ProfileResponse {
  status: number;
  data: Profile;
}
