import { api } from '@/app/lib/api';
import { ChangePasswordRequest, ChangePasswordResponse } from '../types/changePassword';

export const changePassword = async (
  data: ChangePasswordRequest,
): Promise<ChangePasswordResponse> => {
  const response = await api.post('/me/password', data);
  return response.data;
};
