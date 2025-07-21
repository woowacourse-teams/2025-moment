import { LoginFormData } from '@/features/auth/types/login';
import { isLoginFormValid, validateLoginFormData } from './validateLoginForm';

describe('validateLoginFormData', () => {
  it('이메일이 비어있는 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: '',
      password: 'validpassword',
    };

    const result = validateLoginFormData(formData);

    expect(result.email).toBe('이메일을 입력해주세요.');
    expect(result.password).toBe('');
  });

  it('이메일 형식이 잘못된 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'invalid-email',
      password: 'validpassword',
    };

    const result = validateLoginFormData(formData);

    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    expect(result.password).toBe('');
  });

  it('비밀번호가 비어있는 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: '',
    };

    const result = validateLoginFormData(formData);

    expect(result.email).toBe('');
    expect(result.password).toBe('비밀번호를 입력해주세요.');
  });

  it('비밀번호가 6자 미만인 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: '12345',
    };

    const result = validateLoginFormData(formData);

    expect(result.email).toBe('');
    expect(result.password).toBe('비밀번호는 최소 6자 이상이어야 합니다.');
  });

  it('모든 입력이 유효한 경우 빈 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: 'validpassword',
    };

    const result = validateLoginFormData(formData);

    expect(result.email).toBe('');
    expect(result.password).toBe('');
  });

  it('이메일과 비밀번호 모두 잘못된 경우 모든 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'invalid-email',
      password: '123',
    };

    const result = validateLoginFormData(formData);

    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    expect(result.password).toBe('비밀번호는 최소 6자 이상이어야 합니다.');
  });

  it('다양한 유효한 이메일 형식을 허용해야 한다', () => {
    const validEmails = [
      'test@example.com',
      'user.name@example.com',
      'user+tag@example.com',
      'user123@example-site.co.kr',
    ];

    validEmails.forEach(email => {
      const formData: LoginFormData = {
        email,
        password: 'validpassword',
      };

      const result = validateLoginFormData(formData);
      expect(result.email).toBe('');
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
    ];

    invalidEmails.forEach(email => {
      const formData: LoginFormData = {
        email,
        password: 'validpassword',
      };

      const result = validateLoginFormData(formData);
      expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    });
  });
});

describe('isLoginFormValid', () => {
  it('모든 에러가 빈 문자열인 경우 true를 반환해야 한다', () => {
    const errors = {
      email: '',
      password: '',
    };

    const result = isLoginFormValid(errors);

    expect(result).toBe(true);
  });

  it('이메일 에러가 있는 경우 false를 반환해야 한다', () => {
    const errors = {
      email: '이메일을 입력해주세요.',
      password: '',
    };

    const result = isLoginFormValid(errors);

    expect(result).toBe(false);
  });

  it('비밀번호 에러가 있는 경우 false를 반환해야 한다', () => {
    const errors = {
      email: '',
      password: '비밀번호를 입력해주세요.',
    };

    const result = isLoginFormValid(errors);

    expect(result).toBe(false);
  });

  it('모든 에러가 있는 경우 false를 반환해야 한다', () => {
    const errors = {
      email: '이메일을 입력해주세요.',
      password: '비밀번호를 입력해주세요.',
    };

    const result = isLoginFormValid(errors);

    expect(result).toBe(false);
  });

  it('에러 객체가 비어있는 경우 true를 반환해야 한다', () => {
    const errors = {
      email: '',
      password: '',
    };

    const result = isLoginFormValid(errors);

    expect(result).toBe(true);
  });
});
