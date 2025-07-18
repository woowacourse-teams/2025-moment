export interface UserContextType {
  userData: UserData;
  changeUserData: (key: keyof UserData, value: string) => void;
  resetUserData: () => void;
  error: UserError;
}

export interface UserData {
  email: string;
  nickname: string;
}

export interface UserError {
  emailError: string;
  nicknameError: string;
}
