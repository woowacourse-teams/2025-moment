import { ECHO_TYPE } from '../const/echoType';
import { EchoTypeKey } from '../type/echos';

export const echoMapping = (echoType: EchoTypeKey) => {
  return ECHO_TYPE[echoType];
};
