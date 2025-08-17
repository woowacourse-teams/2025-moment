export interface PasswordChangeRequest {
  newPassword: string;
  checkPassword: string;
}

export interface PasswordChangeResponse {
  status: number;
  data: string;
}

export interface PasswordChangeErrors {
  newPassword: string;
  checkPassword: string;
}
