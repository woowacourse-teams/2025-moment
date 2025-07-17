import { useState } from 'react';
import * as S from './SignupStep.styles';

interface FormData {
  username: string;
  password: string;
  confirmPassword: string;
}

export const SignupStep1 = () => {
  const [formData, setFormData] = useState<FormData>({
    username: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState<Partial<FormData>>({});

  const handleInputChange = (field: keyof FormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [field]: e.target.value,
    }));

    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: undefined,
      }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<FormData> = {};

    if (!formData.username.trim()) {
      newErrors.username = '아이디를 입력해주세요.';
    } else if (formData.username.length < 4) {
      newErrors.username = '아이디는 4자 이상이어야 합니다.';
    }

    if (!formData.password.trim()) {
      newErrors.password = '비밀번호를 입력해주세요.';
    } else if (formData.password.length < 6) {
      newErrors.password = '비밀번호는 6자 이상이어야 합니다.';
    }

    if (!formData.confirmPassword.trim()) {
      newErrors.confirmPassword = '비밀번호 확인을 입력해주세요.';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = '비밀번호가 일치하지 않습니다.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  return (
    <S.StepContainer>
      <S.InputGroup>
        <S.Label htmlFor="username">아이디</S.Label>
        <S.Input
          id="username"
          type="text"
          placeholder="아이디를 입력해주세요"
          value={formData.username}
          onChange={handleInputChange('username')}
        />
        {errors.username && <S.ErrorMessage>{errors.username}</S.ErrorMessage>}
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="password">비밀번호</S.Label>
        <S.Input
          id="password"
          type="password"
          placeholder="비밀번호를 입력해주세요"
          value={formData.password}
          onChange={handleInputChange('password')}
        />
        {errors.password && <S.ErrorMessage>{errors.password}</S.ErrorMessage>}
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="confirmPassword">비밀번호 확인</S.Label>
        <S.Input
          id="confirmPassword"
          type="password"
          placeholder="비밀번호를 다시 입력해주세요"
          value={formData.confirmPassword}
          onChange={handleInputChange('confirmPassword')}
        />
        {errors.confirmPassword && <S.ErrorMessage>{errors.confirmPassword}</S.ErrorMessage>}
      </S.InputGroup>
    </S.StepContainer>
  );
};
