import { api } from '@/app/lib/api';
import { MomentWritingStatusResponse } from '../types/moments';

export const getMomentWritingStatus = async (): Promise<MomentWritingStatusResponse> => {
  const response = await api.get('moments/writable/basic');
  return response.data;
};
