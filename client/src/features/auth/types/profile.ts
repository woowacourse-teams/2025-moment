export interface Profile {
  nickname: string;
  level: string;
  expStar: number;
  nextStepExp: number;
}

export interface ProfileResponse {
  status: number;
  data: Profile;
}
