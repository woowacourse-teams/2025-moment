import { api } from '@/app/lib/api';
import { CheckMomentsResponse } from '../types/moments';

export const getCheckMoments = async (): Promise<CheckMomentsResponse> => {
  const response = await api.get('moments/me/creation-status');
  return response.data;
};
