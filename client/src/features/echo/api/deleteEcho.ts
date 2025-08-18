import { api } from '@/app/lib/api';
import { EchoResponse } from '../type/echos';

export const deleteEcho = async (echoId: number): Promise<EchoResponse> => {
  const response = await api.delete(`/echos/${echoId}`);
  return response.data;
};
