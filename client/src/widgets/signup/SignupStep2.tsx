import { useState } from 'react';
import * as S from './SignupStep.styles';

interface FormData {
  nickname: string;
}

export const SignupStep2 = () => {
  const [formData, setFormData] = useState<FormData>({
    nickname: '',
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

    if (!formData.nickname.trim()) {
      newErrors.nickname = '닉네임을 입력해주세요.';
    } else if (formData.nickname.length < 2) {
      newErrors.nickname = '닉네임은 2자 이상이어야 합니다.';
    } else if (formData.nickname.length > 10) {
      newErrors.nickname = '닉네임은 10자 이하여야 합니다.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  return (
    <S.StepContainer>
      <S.InputGroup>
        <S.Label htmlFor="nickname">닉네임</S.Label>
        <S.Input
          id="nickname"
          type="text"
          placeholder="닉네임을 입력해주세요"
          value={formData.nickname}
          onChange={handleInputChange('nickname')}
        />
        {errors.nickname && <S.ErrorMessage>{errors.nickname}</S.ErrorMessage>}
      </S.InputGroup>
    </S.StepContainer>
  );
};
