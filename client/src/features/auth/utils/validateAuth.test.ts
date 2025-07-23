import { LoginFormData } from '@/features/auth/types/login';
import { SignupFormData } from '@/features/auth/types/signup';
import {
  isLoginFormValid,
  isSignupFormValid,
  validateLoginForm,
  validateSignupForm,
} from './validateAuth';

describe('로그인 유효성 검사', () => {
  it('이메일이 비어있는 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: '',
      password: 'validpassword',
    };

    const result = validateLoginForm(formData);

    expect(result.email).toBe('이메일을 입력해주세요.');
    expect(result.password).toBe('');
  });

  it('이메일 형식이 잘못된 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'invalid-email',
      password: 'validpassword',
    };

    const result = validateLoginForm(formData);

    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    expect(result.password).toBe('');
  });

  it('비밀번호가 비어있는 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: '',
    };

    const result = validateLoginForm(formData);

    expect(result.email).toBe('');
    expect(result.password).toBe('비밀번호를 입력해주세요.');
  });

  it('비밀번호가 4자 미만인 경우 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: '123',
    };

    const result = validateLoginForm(formData);

    expect(result.email).toBe('');
    expect(result.password).toBe('비밀번호는 최소 4자 이상이어야 합니다.');
  });

  it('모든 입력이 유효한 경우 빈 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: 'validpassword',
    };

    const result = validateLoginForm(formData);

    expect(result.email).toBe('');
    expect(result.password).toBe('');
  });

  it('이메일과 비밀번호 모두 잘못된 경우 모든 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'invalid-email',
      password: '12',
    };

    const result = validateLoginForm(formData);

    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    expect(result.password).toBe('비밀번호는 최소 4자 이상이어야 합니다.');
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

      const result = validateLoginForm(formData);
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

      const result = validateLoginForm(formData);
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

  it('에러 객체가 비어있지 않은 경우 false를 반환해야 한다', () => {
    const errors = {
      email: '이메일을 입력해주세요.',
      password: '비밀번호를 입력해주세요.',
    };

    const result = isLoginFormValid(errors);

    expect(result).toBe(false);
  });
});

describe('회원가입 유효성 검사', () => {
  it('이메일이 비어있는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: '',
      password: 'validpassword',
      rePassword: 'validpassword',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('이메일을 입력해주세요.');
    expect(result.password).toBe('');
    expect(result.rePassword).toBe('');
    expect(result.nickname).toBe('');
  });

  it('이메일 형식이 잘못된 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'invalid-email',
      password: 'validpassword',
      rePassword: 'validpassword',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    expect(result.password).toBe('');
    expect(result.rePassword).toBe('');
    expect(result.nickname).toBe('');
  });

  it('비밀번호가 비어있는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: '',
      rePassword: '',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('');
    expect(result.password).toBe('비밀번호를 입력해주세요.');
    expect(result.rePassword).toBe('비밀번호를 입력해주세요.');
    expect(result.nickname).toBe('');
  });

  it('비밀번호가 4자 미만인 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: '123',
      rePassword: '123',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('');
    expect(result.password).toBe('비밀번호는 최소 4자 이상이어야 합니다.');
    expect(result.rePassword).toBe('');
    expect(result.nickname).toBe('');
  });

  it('비밀번호 확인이 비어있는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'validpassword',
      rePassword: '',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('');
    expect(result.password).toBe('');
    expect(result.rePassword).toBe('비밀번호를 입력해주세요.');
    expect(result.nickname).toBe('');
  });

  it('비밀번호와 비밀번호 확인이 일치하지 않는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'validpassword',
      rePassword: 'differentpassword',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('');
    expect(result.password).toBe('');
    expect(result.rePassword).toBe('비밀번호가 일치하지 않습니다.');
    expect(result.nickname).toBe('');
  });

  it('닉네임이 비어있는 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'validpassword',
      rePassword: 'validpassword',
      nickname: '',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('');
    expect(result.password).toBe('');
    expect(result.rePassword).toBe('');
    expect(result.nickname).toBe('닉네임을 입력해주세요.');
  });

  it('닉네임이 2자 미만인 경우 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'validpassword',
      rePassword: 'validpassword',
      nickname: 'a',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('');
    expect(result.password).toBe('');
    expect(result.rePassword).toBe('');
    expect(result.nickname).toBe('닉네임은 최소 2자 이상이어야 합니다.');
  });

  it('모든 입력이 유효한 경우 빈 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'validpassword',
      rePassword: 'validpassword',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('');
    expect(result.password).toBe('');
    expect(result.rePassword).toBe('');
    expect(result.nickname).toBe('');
  });

  it('모든 필드에 에러가 있는 경우 모든 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'invalid-email',
      password: '12',
      rePassword: 'different',
      nickname: 'a',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    expect(result.password).toBe('비밀번호는 최소 4자 이상이어야 합니다.');
    expect(result.rePassword).toBe('비밀번호가 일치하지 않습니다.');
    expect(result.nickname).toBe('닉네임은 최소 2자 이상이어야 합니다.');
  });

  it('다양한 유효한 이메일 형식을 허용해야 한다', () => {
    const validEmails = [
      'test@example.com',
      'user.name@example.com',
      'user+tag@example.com',
      'user123@example-site.co.kr',
    ];

    validEmails.forEach(email => {
      const signupData: SignupFormData = {
        email,
        password: 'validpassword',
        rePassword: 'validpassword',
        nickname: 'testnick',
      };

      const result = validateSignupForm(signupData);
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
      const signupData: SignupFormData = {
        email,
        password: 'validpassword',
        rePassword: 'validpassword',
        nickname: 'testnick',
      };

      const result = validateSignupForm(signupData);
      expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    });
  });

  it('다양한 유효한 닉네임을 허용해야 한다', () => {
    const validNicknames = ['ab', 'test', 'testuser123', '한글닉네임', '가나다라마바사'];

    validNicknames.forEach(nickname => {
      const signupData: SignupFormData = {
        email: 'test@example.com',
        password: 'validpassword',
        rePassword: 'validpassword',
        nickname,
      };

      const result = validateSignupForm(signupData);
      expect(result.nickname).toBe('');
    });
  });
});

describe('isSignupFormValid', () => {
  it('모든 에러가 빈 문자열인 경우 true를 반환해야 한다', () => {
    const errors = {
      email: '',
      password: '',
      rePassword: '',
      nickname: '',
    };

    const result = isSignupFormValid(errors);

    expect(result).toBe(true);
  });

  it('이메일 에러가 있는 경우 false를 반환해야 한다', () => {
    const errors = {
      email: '이메일을 입력해주세요.',
      password: '',
      rePassword: '',
      nickname: '',
    };

    const result = isSignupFormValid(errors);

    expect(result).toBe(false);
  });

  it('비밀번호 에러가 있는 경우 false를 반환해야 한다', () => {
    const errors = {
      email: '',
      password: '비밀번호를 입력해주세요.',
      rePassword: '',
      nickname: '',
    };

    const result = isSignupFormValid(errors);

    expect(result).toBe(false);
  });

  it('비밀번호 확인 에러가 있는 경우 false를 반환해야 한다', () => {
    const errors = {
      email: '',
      password: '',
      rePassword: '비밀번호가 일치하지 않습니다.',
      nickname: '',
    };

    const result = isSignupFormValid(errors);

    expect(result).toBe(false);
  });

  it('닉네임 에러가 있는 경우 false를 반환해야 한다', () => {
    const errors = {
      email: '',
      password: '',
      rePassword: '',
      nickname: '닉네임을 입력해주세요.',
    };

    const result = isSignupFormValid(errors);

    expect(result).toBe(false);
  });

  it('모든 에러가 있는 경우 false를 반환해야 한다', () => {
    const errors = {
      email: '이메일을 입력해주세요.',
      password: '비밀번호를 입력해주세요.',
      rePassword: '비밀번호가 일치하지 않습니다.',
      nickname: '닉네임을 입력해주세요.',
    };

    const result = isSignupFormValid(errors);

    expect(result).toBe(false);
  });
});
