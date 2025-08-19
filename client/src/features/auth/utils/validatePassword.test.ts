import { LoginFormData } from '@/features/auth/types/login';
import { SignupFormData } from '@/features/auth/types/signup';
import {
  validateLoginForm,
  validatePassword,
  validateChangePasswordForm,
  validateRePassword,
  validateSignupForm,
} from './validateAuth';
import { ChangePasswordRequest } from '../types/changePassword';

describe('비밀번호 유효성 검사', () => {
  it('비밀번호가 비어있는 경우 에러를 반환해야 한다', () => {
    const password = '';
    const result = validatePassword(password);
    expect(result).toBe('비밀번호를 입력해주세요.');
  });

  it('비밀번호가 규칙에 맞는 경우 빈 문자열을 반환해야 한다', () => {
    const password = 'Valid123!';
    const result = validatePassword(password);
    expect(result).toBe('');
  });

  it('비밀번호가 규칙에 맞지 않는 경우 에러를 반환해야 한다', () => {
    const password = '123';
    const result = validatePassword(password);
    expect(result).toBe(
      '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
    );
  });

  it('다양한 유효한 비밀번호를 허용해야 한다', () => {
    const validPasswords = [
      'Valid123!',
      'Password1@',
      'test1234#',
      'MyPass9$',
      'secure2023&',
      'hello123*',
      'Welcome1(',
      'Test456)',
    ];

    validPasswords.forEach(password => {
      const result = validatePassword(password);
      expect(result).toBe('');
    });
  });

  it('다양한 잘못된 비밀번호를 거부해야 한다', () => {
    const invalidPasswords = [
      '123456', // 숫자만
      'password', // 소문자만
      'PASSWORD123', // 대문자+숫자 (소문자 없음)
      'Password!', // 숫자 없음
      'Password123', // 특수문자 없음
      '1234567', // 8자 미만
      'VeryLongPassword123!@#$%', // 16자 초과
      'valid123', // 특수문자 없음
      'VALID123!', // 소문자 없음
      'Valid!@#', // 숫자 없음
    ];

    invalidPasswords.forEach(password => {
      const result = validatePassword(password);
      expect(result).toBe(
        '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
      );
    });
  });
});

describe('비밀번호 확인 유효성 검사', () => {
  it('비밀번호 확인이 비어있는 경우 에러를 반환해야 한다', () => {
    const password = 'Valid123!';
    const rePassword = '';
    const result = validateRePassword(password, rePassword);
    expect(result).toBe('비밀번호를 입력해주세요.');
  });

  it('비밀번호와 확인이 일치하는 경우 빈 문자열을 반환해야 한다', () => {
    const password = 'Valid123!';
    const rePassword = 'Valid123!';
    const result = validateRePassword(password, rePassword);
    expect(result).toBe('');
  });

  it('비밀번호와 확인이 일치하지 않는 경우 에러를 반환해야 한다', () => {
    const password = 'Valid123!';
    const rePassword = 'Different456@';
    const result = validateRePassword(password, rePassword);
    expect(result).toBe('비밀번호가 일치하지 않습니다.');
  });
});

describe('로그인 폼에서 비밀번호 유효성 검사', () => {
  it('비밀번호가 비어있는 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: '',
    };

    const result = validateLoginForm(formData);
    expect(result.password).toBe('비밀번호를 입력해주세요.');
  });

  it('비밀번호가 규칙에 맞지 않는 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: '123',
    };

    const result = validateLoginForm(formData);
    expect(result.password).toBe(
      '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
    );
  });

  it('비밀번호가 유효한 경우 빈 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: 'Valid123!',
    };

    const result = validateLoginForm(formData);
    expect(result.password).toBe('');
  });
});

describe('회원가입 폼에서 비밀번호 유효성 검사', () => {
  it('비밀번호가 비어있는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: '',
      rePassword: '',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);
    expect(result.password).toBe('비밀번호를 입력해주세요.');
    expect(result.rePassword).toBe('비밀번호를 입력해주세요.');
  });

  it('비밀번호가 규칙에 맞지 않는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: '123',
      rePassword: '123',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);
    expect(result.password).toBe(
      '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
    );
  });

  it('비밀번호 확인이 비어있는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'Valid123!',
      rePassword: '',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);
    expect(result.rePassword).toBe('비밀번호를 입력해주세요.');
  });

  it('비밀번호와 비밀번호 확인이 일치하지 않는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'Valid123!',
      rePassword: 'Different456@',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);
    expect(result.rePassword).toBe('비밀번호가 일치하지 않습니다.');
  });

  it('비밀번호가 모두 유효한 경우 빈 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'Valid123!',
      rePassword: 'Valid123!',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);
    expect(result.password).toBe('');
    expect(result.rePassword).toBe('');
  });
});

describe('비밀번호 변경 폼에서 비밀번호 유효성 검사', () => {
  it('새 비밀번호가 비어있는 경우 에러를 반환해야 한다', () => {
    const changePasswordData: ChangePasswordRequest = {
      newPassword: '',
      checkedPassword: 'Valid123!',
    };

    const result = validateChangePasswordForm(changePasswordData);
    expect(result.newPassword).toBe('비밀번호를 입력해주세요.');
  });

  it('새 비밀번호가 규칙에 맞지 않는 경우 에러를 반환해야 한다', () => {
    const changePasswordData: ChangePasswordRequest = {
      newPassword: '123',
      checkedPassword: '123',
    };

    const result = validateChangePasswordForm(changePasswordData);
    expect(result.newPassword).toBe(
      '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
    );
  });

  it('새 비밀번호 확인이 비어있는 경우 에러를 반환해야 한다', () => {
    const changePasswordData: ChangePasswordRequest = {
      newPassword: 'Valid123!',
      checkedPassword: '',
    };

    const result = validateChangePasswordForm(changePasswordData);
    expect(result.checkedPassword).toBe('비밀번호를 입력해주세요.');
  });

  it('새 비밀번호와 비밀번호 확인이 일치하지 않는 경우 에러를 반환해야 한다', () => {
    const changePasswordData: ChangePasswordRequest = {
      newPassword: 'Valid123!',
      checkedPassword: 'Different456@',
    };

    const result = validateChangePasswordForm(changePasswordData);
    expect(result.checkedPassword).toBe('비밀번호가 일치하지 않습니다.');
  });

  it('새 비밀번호와 비밀번호 확인이 모두 유효한 경우 빈 에러를 반환해야 한다', () => {
    const changePasswordData: ChangePasswordRequest = {
      newPassword: 'Valid123!',
      checkedPassword: 'Valid123!',
    };

    const result = validateChangePasswordForm(changePasswordData);
    expect(result.newPassword).toBe('');
    expect(result.checkedPassword).toBe('');
  });
});
