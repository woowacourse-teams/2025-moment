export interface ChangePasswordRequest {
  newPassword: string;
  checkedPassword: string;
}

export interface ChangePasswordResponse {
  status: number;
  data: string;
}

export interface ChangePasswordErrors {
  newPassword: string;
  checkedPassword: string;
}
