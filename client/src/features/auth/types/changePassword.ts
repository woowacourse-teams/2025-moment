export interface ChangePasswordRequest {
  newPassword: string;
  checkPassword: string;
}

export interface ChangePasswordResponse {
  status: number;
  data: string;
}

export interface ChangePasswordErrors {
  newPassword: string;
  checkPassword: string;
}
