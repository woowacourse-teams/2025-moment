import { api } from '@/app/lib/api';
import { EchoRequest, EchoResponse } from '@/features/echo/type/echos';

export const sendEcho = async (echos: EchoRequest): Promise<EchoResponse> => {
  const response = await api.post('/echos', echos);
  return response.data;
};
