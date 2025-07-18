import * as S from './SignupStep3.styles';
import { useUserContext } from '@/features/auth/context/useUserContext';
import { Button } from '@/shared/ui/Button';

export const SignupStep3 = () => {
  const { userData } = useUserContext();

  const handleSignup = () => {
    // TODO: 실제 회원가입 API 호출
    console.log('회원가입 요청:', userData);
  };

  return (
    <S.StepContainer>
      <S.InfoContainer>
        <S.InfoItem>
          <S.InfoLabel>이메일</S.InfoLabel>
          <S.InfoValue>{userData.email}</S.InfoValue>
        </S.InfoItem>
        <S.InfoItem>
          <S.InfoLabel>닉네임</S.InfoLabel>
          <S.InfoValue>{userData.nickname}</S.InfoValue>
        </S.InfoItem>
      </S.InfoContainer>

      <S.Description>
        <span>입력하신 정보가 맞는지 확인해 주세요.</span>
        <span>확인 후 회원가입 버튼을 눌러주세요.</span>
      </S.Description>

      <S.ButtonContainer>
        <Button
          title="회원가입"
          variant="primary"
          onClick={handleSignup}
        />
      </S.ButtonContainer>
    </S.StepContainer>
  );
};
