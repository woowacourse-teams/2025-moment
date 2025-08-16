import { ECHO_TYPE } from '../const/echoType';

export const echoMapping = (echoType: string) => {
  return ECHO_TYPE[echoType as keyof typeof ECHO_TYPE] || echoType;
};
