import { CheckCircle } from 'lucide-react';
import * as S from './SignupStep3.styles';
import { useUserContext } from '@/features/auth/context/useUserContext';

export const SignupStep3 = () => {
  const { userData } = useUserContext();

  return (
    <S.StepContainer>
      <S.SuccessIcon>
        <CheckCircle />
      </S.SuccessIcon>

      <S.Title>가입완료!</S.Title>

      <S.Description>
        <span>회원가입이 성공적으로 완료되었습니다.</span>
        <span>당신의 이야기가 공감 받는 순간,</span>
        <span>Moment에 오신 것을 환영합니다.</span>
      </S.Description>
    </S.StepContainer>
  );
};
