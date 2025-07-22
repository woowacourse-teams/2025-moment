import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const validateEmail = (email: string): string => {
  if (!email) {
    return '이메일을 입력해주세요.';
  } else if (!EMAIL_REGEX.test(email)) {
    return '올바른 이메일 형식을 입력해주세요.';
  }
  return '';
};

export const validatePassword = (password: string): string => {
  if (!password) {
    return '비밀번호를 입력해주세요.';
  } else if (password.length < 4) {
    return '비밀번호는 최소 4자 이상이어야 합니다.';
  }
  return '';
};

export const validateRePassword = (password: string, rePassword: string): string => {
  if (!rePassword) {
    return '비밀번호를 입력해주세요.';
  } else if (rePassword !== password) {
    return '비밀번호가 일치하지 않습니다.';
  }
  return '';
};

export const validateNickname = (nickname: string): string => {
  if (!nickname) {
    return '닉네임을 입력해주세요.';
  } else if (nickname.length < 2) {
    return '닉네임은 최소 2자 이상이어야 합니다.';
  }
  return '';
};

export const validateSingleField = (
  field: keyof SignupFormData,
  value: string,
  signupData: SignupFormData,
): string => {
  switch (field) {
    case 'email':
      return validateEmail(value);
    case 'password':
      return validatePassword(value);
    case 'rePassword':
      return validateRePassword(signupData.password, value);
    case 'nickname':
      return validateNickname(value);
    default:
      return '';
  }
};

export const validateSignupData = ({
  signupData,
  setErrors,
}: {
  signupData: SignupFormData;
  setErrors: (errors: SignupErrors) => void;
}) => {
  const errors: SignupErrors = {
    email: validateEmail(signupData.email),
    password: validatePassword(signupData.password),
    rePassword: validateRePassword(signupData.password, signupData.rePassword),
    nickname: validateNickname(signupData.nickname),
  };

  setErrors(errors);
};

export const isSignupFormValid = (errors: SignupErrors): boolean => {
  return Object.values(errors).every(error => error === '');
};
