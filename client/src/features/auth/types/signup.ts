export interface SignupContextType {
  signupData: SignupData;
  changeSignupData: (key: keyof SignupData, value: string) => void;
  resetSignupData: () => void;
}

export interface SignupData {
  email: string;
  password: string;
  rePassword: string;
  nickname: string;
}
