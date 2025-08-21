import { LoginError, LoginFormData } from '@/features/auth/types/login';
import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';
import { ChangePasswordErrors, ChangePasswordRequest } from '../types/changePassword';

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const NICKNAME_REGEX = /[`~!@#$%^&*()_|+\-=?;:'",.<>\{\}\[\]\\\/]/gim;
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()])[a-zA-Z\d!@#$%^&*()]{8,16}$/;

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
  } else if (!PASSWORD_REGEX.test(password)) {
    return '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.';
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
  } else if (nickname.length > 15) {
    return '닉네임은 최대 15자 이하여야 합니다.';
  } else if (NICKNAME_REGEX.test(nickname)) {
    return '닉네임은 특수문자를 포함할 수 없습니다.';
  }
  return '';
};

export const validateSignupField = (
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

export const validateChangePasswordField = (
  field: keyof ChangePasswordRequest,
  value: string,
  formData: ChangePasswordRequest,
): string => {
  switch (field) {
    case 'newPassword':
      return validatePassword(value);
    case 'checkedPassword':
      return validateRePassword(formData.newPassword, value);
    default:
      return '';
  }
};

export const validateLoginForm = (data: LoginFormData): LoginError => {
  return {
    email: validateEmail(data.email),
    password: validatePassword(data.password),
  };
};

export const validateSignupForm = (data: SignupFormData): SignupErrors => {
  return {
    email: validateEmail(data.email),
    password: validatePassword(data.password),
    rePassword: validateRePassword(data.password, data.rePassword),
    nickname: validateNickname(data.nickname),
  };
};

export const validateChangePasswordForm = (data: ChangePasswordRequest): ChangePasswordErrors => {
  return {
    newPassword: validatePassword(data.newPassword),
    checkedPassword: validateRePassword(data.newPassword, data.checkedPassword),
  };
};

export const isLoginFormValid = (errors: LoginError): boolean => {
  return Object.values(errors).every(error => error === '');
};

export const isSignupFormValid = (errors: SignupErrors): boolean => {
  return Object.values(errors).every(error => error === '');
};

export const isLoginFormEmpty = (data: LoginFormData): boolean => {
  return Object.values(data).every(value => value === '');
};

export const isChangePasswordFormValid = (errors: ChangePasswordErrors): boolean => {
  return Object.values(errors).every(error => error === '');
};
