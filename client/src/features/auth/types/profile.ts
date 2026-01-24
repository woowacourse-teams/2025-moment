export interface Profile {
  id: number;
  nickname: string;
  expStar: number;
}

export interface ProfileResponse {
  status: number;
  data: Profile;
}
