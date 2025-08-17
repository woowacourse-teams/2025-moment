import { api } from '@/app/lib/api';
import { PasswordChangeRequest, PasswordChangeResponse } from '../types/passwordChange';

export const passwordChange = async (
  data: PasswordChangeRequest,
): Promise<PasswordChangeResponse> => {
  const response = await api.post('api/v1/users/me/password', data);
  return response.data;
};
