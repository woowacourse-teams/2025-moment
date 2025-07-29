import { LoginFormData } from '@/features/auth/types/login';
import { SignupFormData } from '@/features/auth/types/signup';
import {
  isDataEmpty,
  isLoginFormValid,
  isSignupFormValid,
  validateEmail,
  validateLoginForm,
  validateNickname,
  validatePassword,
  validateSignupForm,
} from './validateAuth';

describe('이메일과 비밀번호 통합 유효성 검사', () => {
  describe('개별 함수 통합 테스트', () => {
    it('유효한 이메일과 유효한 비밀번호 조합', () => {
      const email = 'test@example.com';
      const password = 'Valid123!';

      const emailResult = validateEmail(email);
      const passwordResult = validatePassword(password);

      expect(emailResult).toBe('');
      expect(passwordResult).toBe('');
    });

    it('유효한 이메일과 무효한 비밀번호 조합', () => {
      const email = 'test@example.com';
      const password = '123';

      const emailResult = validateEmail(email);
      const passwordResult = validatePassword(password);

      expect(emailResult).toBe('');
      expect(passwordResult).toBe(
        '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
      );
    });

    it('무효한 이메일과 유효한 비밀번호 조합', () => {
      const email = 'invalid-email';
      const password = 'Valid123!';

      const emailResult = validateEmail(email);
      const passwordResult = validatePassword(password);

      expect(emailResult).toBe('올바른 이메일 형식을 입력해주세요.');
      expect(passwordResult).toBe('');
    });

    it('무효한 이메일과 무효한 비밀번호 조합', () => {
      const email = 'invalid-email';
      const password = '123';

      const emailResult = validateEmail(email);
      const passwordResult = validatePassword(password);

      expect(emailResult).toBe('올바른 이메일 형식을 입력해주세요.');
      expect(passwordResult).toBe(
        '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
      );
    });

    it('빈 이메일과 빈 비밀번호 조합', () => {
      const email = '';
      const password = '';

      const emailResult = validateEmail(email);
      const passwordResult = validatePassword(password);

      expect(emailResult).toBe('이메일을 입력해주세요.');
      expect(passwordResult).toBe('비밀번호를 입력해주세요.');
    });
  });

  describe('다양한 이메일-비밀번호 조합 테스트', () => {
    const testCases = [
      {
        description: '모든 유효한 조합들',
        email: 'user@domain.com',
        password: 'StrongPass1!',
        expectedEmailError: '',
        expectedPasswordError: '',
      },
      {
        description: '특수 문자 포함 이메일과 강력한 비밀번호',
        email: 'user.name+tag@example-site.co.kr',
        password: 'MySecure123@',
        expectedEmailError: '',
        expectedPasswordError: '',
      },
      {
        description: '유효한 이메일과 특수문자 없는 비밀번호',
        email: 'test@example.com',
        password: 'Password123',
        expectedEmailError: '',
        expectedPasswordError:
          '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
      },
      {
        description: '도메인 확장자가 짧은 이메일과 유효한 비밀번호',
        email: 'test@example.c',
        password: 'Valid123!',
        expectedEmailError: '올바른 이메일 형식을 입력해주세요.',
        expectedPasswordError: '',
      },
      {
        description: '공백 포함 이메일과 너무 긴 비밀번호',
        email: 'test @example.com',
        password: 'VeryLongPasswordThatExceeds16Characters123!',
        expectedEmailError: '올바른 이메일 형식을 입력해주세요.',
        expectedPasswordError:
          '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
      },
    ];

    testCases.forEach(
      ({ description, email, password, expectedEmailError, expectedPasswordError }) => {
        it(description, () => {
          const emailResult = validateEmail(email);
          const passwordResult = validatePassword(password);

          expect(emailResult).toBe(expectedEmailError);
          expect(passwordResult).toBe(expectedPasswordError);
        });
      },
    );
  });
});

describe('닉네임 유효성 검사', () => {
  it('닉네임이 비어있는 경우 에러를 반환해야 한다', () => {
    const nickname = '';
    const result = validateNickname(nickname);
    expect(result).toBe('닉네임을 입력해주세요.');
  });

  it('닉네임이 2자 미만인 경우 에러를 반환해야 한다', () => {
    const nickname = 'a';
    const result = validateNickname(nickname);
    expect(result).toBe('닉네임은 최소 2자 이상이어야 합니다.');
  });

  it('닉네임이 유효한 경우 빈 문자열을 반환해야 한다', () => {
    const nickname = 'testnick';
    const result = validateNickname(nickname);
    expect(result).toBe('');
  });

  it('다양한 유효한 닉네임을 허용해야 한다', () => {
    const validNicknames = ['ab', 'test', 'testuser123', '한글닉네임', '가나다라마바사'];

    validNicknames.forEach(nickname => {
      const result = validateNickname(nickname);
      expect(result).toBe('');
    });
  });
});

describe('로그인 폼 전체 유효성 검사', () => {
  it('모든 입력이 유효한 경우 빈 에러를 반환해야 한다', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: 'Valid123!',
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
    expect(result.password).toBe(
      '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
    );
  });

  it('이메일만 잘못된 경우', () => {
    const formData: LoginFormData = {
      email: 'invalid@',
      password: 'Valid123!',
    };

    const result = validateLoginForm(formData);

    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    expect(result.password).toBe('');
  });

  it('비밀번호만 잘못된 경우', () => {
    const formData: LoginFormData = {
      email: 'test@example.com',
      password: 'weak',
    };

    const result = validateLoginForm(formData);

    expect(result.email).toBe('');
    expect(result.password).toBe(
      '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
    );
  });
});

describe('회원가입 폼 전체 유효성 검사', () => {
  it('모든 입력이 유효한 경우 빈 에러를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: 'test@example.com',
      password: 'Valid123!',
      rePassword: 'Valid123!',
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
    expect(result.password).toBe(
      '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
    );
    expect(result.rePassword).toBe('비밀번호가 일치하지 않습니다.');
    expect(result.nickname).toBe('닉네임은 최소 2자 이상이어야 합니다.');
  });

  it('이메일과 비밀번호만 잘못된 경우', () => {
    const signupData: SignupFormData = {
      email: 'invalid@',
      password: 'weak',
      rePassword: 'weak',
      nickname: 'testnick',
    };

    const result = validateSignupForm(signupData);

    expect(result.email).toBe('올바른 이메일 형식을 입력해주세요.');
    expect(result.password).toBe(
      '비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자(!@#$%^&*())를 포함해야 합니다.',
    );
    expect(result.rePassword).toBe('');
    expect(result.nickname).toBe('');
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

describe('isDataEmpty', () => {
  it('로그인 데이터가 모두 비어있는 경우 true를 반환해야 한다', () => {
    const loginData: LoginFormData = {
      email: '',
      password: '',
    };

    const result = isDataEmpty(loginData);

    expect(result).toBe(true);
  });

  it('로그인 데이터가 일부라도 채워져 있는 경우 false를 반환해야 한다', () => {
    const loginData: LoginFormData = {
      email: 'test@example.com',
      password: '',
    };

    const result = isDataEmpty(loginData);

    expect(result).toBe(false);
  });

  it('회원가입 데이터가 모두 비어있는 경우 true를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: '',
      password: '',
      rePassword: '',
      nickname: '',
    };

    const result = isDataEmpty(signupData);

    expect(result).toBe(true);
  });

  it('회원가입 데이터가 일부라도 채워져 있는 경우 false를 반환해야 한다', () => {
    const signupData: SignupFormData = {
      email: '',
      password: '',
      rePassword: '',
      nickname: 'testnick',
    };

    const result = isDataEmpty(signupData);

    expect(result).toBe(false);
  });
});
