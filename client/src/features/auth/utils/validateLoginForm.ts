import { LoginError, LoginFormData } from '@/features/auth/types/login';

export const validateLoginFormData = (data: LoginFormData): LoginError => {
  const errors: LoginError = {
    email: '',
    password: '',
  };

  if (!data.email) {
    errors.email = '이메일을 입력해주세요.';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
    errors.email = '올바른 이메일 형식을 입력해주세요.';
  }

  if (!data.password) {
    errors.password = '비밀번호를 입력해주세요.';
  } else if (data.password.length < 6) {
    errors.password = '비밀번호는 최소 6자 이상이어야 합니다.';
  }

  return errors;
};

export const isLoginFormValid = (errors: LoginError): boolean => {
  return Object.values(errors).every(error => error === '');
};
