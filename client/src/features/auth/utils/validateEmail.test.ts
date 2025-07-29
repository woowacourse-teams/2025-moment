import { LoginFormData } from '@/features/auth/types/login';
import { SignupFormData } from '@/features/auth/types/signup';
import { validateEmail, validateLoginForm, validateSignupForm } from './validateAuth';

describe('이메일 유효성 검사', () => {
  it('이메일이 비어있는 경우 에러를 반환해야 한다', () => {
    const email = '';
    const result = validateEmail(email);
    expect(result).toBe('이메일을 입력해주세요.');
  });

  it('이메일 형식이 올바른 경우 빈 문자열을 반환해야 한다', () => {
    const email = 'test@example.com';
    const result = validateEmail(email);
    expect(result).toBe('');
  });

  it('이메일 형식이 잘못된 경우 에러를 반환해야 한다', () => {
    const email = 'invalid-email';
    const result = validateEmail(email);
    expect(result).toBe('올바른 이메일 형식을 입력해주세요.');
  });

  it('다양한 유효한 이메일 형식을 허용해야 한다', () => {
    const validEmails = [
      'test@example.com',
      'user.name@example.com',
      'user123@example-site.co.kr',
      'test_user@domain.org',
      'user+tag@example.com',
      'email123@test-domain.com',
    ];

    validEmails.forEach(email => {
      const result = validateEmail(email);
      expect(result).toBe('');
    });
  });

  it('다양한 잘못된 이메일 형식을 거부해야 한다', () => {
    const invalidEmails = [
      'invalid-email',
      '@example.com',
      'test@',
      'test.example.com',
      'test @example.com',
      'test@example',
      'test@example.c',
      'test@@example.com',
      'test@.com',
    ];

    invalidEmails.forEach(email => {
      const result = validateEmail(email);
      expect(result).toBe('올바른 이메일 형식을 입력해주세요.');
    });
  });
});

describe('로그인 폼에서 이메일 유효성 검사', () => {
  it('이메일이 비어있는 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: '',
      password: 'Valid123!',
    };

    const result = validateLoginForm(formData);
    expect(result.email).toBe('이메일을 입력해주세요.');
  });

  it('이메일 형식이 잘못된 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'invalid-email',
      password: 'Valid123!',
    };

    const result = validateLoginForm(formData);
    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
  });

  it('이메일이 유효한 경우 빈 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: 'Valid123!',
    };

    const result = validateLoginForm(formData);
    expect(result.email).toBe('');
  });
});

describe('회원가입 폼에서 이메일 유효성 검사', () => {
  it('이메일이 비어있는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: '',
      password: 'Valid123!',
      rePassword: 'Valid123!',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);
    expect(result.email).toBe('이메일을 입력해주세요.');
  });

  it('이메일 형식이 잘못된 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'invalid-email',
      password: 'Valid123!',
      rePassword: 'Valid123!',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);
    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
  });

  it('이메일이 유효한 경우 빈 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'Valid123!',
      rePassword: 'Valid123!',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);
    expect(result.email).toBe('');
  });
});
