import { api } from '@/app/lib/api';
import { ChangeNicknameRequest } from '../types/changeNickname';

export const changeNickname = async ({ newNickname }: ChangeNicknameRequest) => {
  const response = await api.post('/me/nickname', { newNickname });
  return response.data;
};
