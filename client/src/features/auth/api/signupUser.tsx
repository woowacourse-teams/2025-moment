import { api } from '@/app/lib/api';
import { SignupRequest, SignupResponse } from '../types/signup';

export const signupUser = async (signupData: SignupRequest): Promise<SignupResponse> => {
  const response = await api.post('/users/signup', signupData);
  return response.data;
};
