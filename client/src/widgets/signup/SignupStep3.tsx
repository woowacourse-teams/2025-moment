import { SignupFormData } from '@/features/auth/types/signup';
import { Button } from '@/shared/ui/button/Button';
import { useEffect } from 'react';
import * as S from './SignupStep3.styles';

interface SignupStep3Props {
  signupData: SignupFormData;
  handleClick: () => void;
  onEnter?: () => void;
}

export const SignupStep3 = ({ signupData, handleClick, onEnter }: SignupStep3Props) => {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Enter' && onEnter) {
        e.preventDefault();
        onEnter();
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onEnter]);

  return (
    <S.StepContainer>
      <S.InfoContainer>
        <S.InfoItem>
          <S.InfoLabel>이메일</S.InfoLabel>
          <S.InfoValue>{signupData.email}</S.InfoValue>
        </S.InfoItem>
        <S.InfoItem>
          <S.InfoLabel>닉네임</S.InfoLabel>
          <S.InfoValue>{signupData.nickname}</S.InfoValue>
        </S.InfoItem>
      </S.InfoContainer>

      <S.Description>
        <span>입력하신 정보가 맞는지 확인해 주세요.</span>
        <span>확인 후 회원가입 버튼을 눌러주세요.</span>
      </S.Description>

      <S.ButtonContainer>
        <Button title="회원가입" variant="primary" onClick={handleClick} />
      </S.ButtonContainer>
    </S.StepContainer>
  );
};
