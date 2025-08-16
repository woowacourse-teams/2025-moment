import { ECHO_TYPE } from '../const/echoType';

export type EchoTypeKey = keyof typeof ECHO_TYPE;

export interface EchoRequest {
  echoTypes: EchoTypeKey[];
  commentId: number;
}

export interface EchoResponse {
  status: number;
  data: Echos[];
}

export interface Echos {
  id: number;
  echoType: string;
}
