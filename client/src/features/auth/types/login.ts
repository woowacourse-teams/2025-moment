export interface LoginFormData {
  email: string;
  password: string;
}

export interface LoginError {
  email: string;
  password: string;
}

export interface LoginResponse {
  status: number;
  data: string;
}
