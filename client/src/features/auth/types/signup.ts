export interface SignupContextType {
  signupData: SignupData;
  changeSignupData: (key: keyof SignupData, value: string) => void;
  resetSignupData: () => void;
  error: SignupError;
}

export interface SignupData {
  email: string;
  password: string;
  rePassword: string;
  nickname: string;
}

export interface SignupError {
  emailError: string;
  passwordError: string;
  rePasswordError: string;
  nicknameError: string;
}
