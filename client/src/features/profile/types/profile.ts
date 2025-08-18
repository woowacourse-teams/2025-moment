export interface Profile {
  nickname: string;
  email: string;
  availableStar: number;
  level: string;
  expStar: number;
  nextStepExp: number;
  loginType: string;
}

export interface ProfileResponse {
  status: number;
  data: Profile;
}
