export interface AppleLoginRequest {
  identityToken: string;
}

export interface AppleLoginResponse {
  status: number;
  data: string;
}
