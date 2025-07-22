export interface SignupFormData {
  email: string;
  password: string;
  rePassword: string;
  nickname: string;
}

export interface SignupErrors {
  email: string;
  password: string;
  rePassword: string;
  nickname: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  rePassword: string;
  nickname: string;
}

export interface SignupResponse {
  status: number;
  data: string;
}

export interface SignupError {
  code: string;
  message: string;
  status: string;
}
