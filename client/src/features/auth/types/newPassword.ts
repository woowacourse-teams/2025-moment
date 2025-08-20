export interface NewPassword {
  email: string;
  token: string;
  newPassword: string;
  newPasswordCheck: string;
}

export interface NewPasswordErrors {
  newPassword: string;
  newPasswordCheck: string;
}
