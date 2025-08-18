import { api } from '@/app/lib/api';
import { MomentExtraWritableResponse } from '../types/moments';

export const getMomentExtraWritable = async (): Promise<MomentExtraWritableResponse> => {
  const response = await api.get('/moments/writable/extra');
  return response.data;
};
