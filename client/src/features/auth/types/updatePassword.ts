export interface UpdatePassword {
  email: string;
  token: string;
  newPassword: string;
  newPasswordCheck: string;
}
