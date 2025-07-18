import { useSignupContext } from '@/features/auth/context/useUserContext';
import * as S from './SignupStep.styles';

export const SignupStep2 = () => {
  const { signupData, changeSignupData, error } = useSignupContext();

  return (
    <S.StepContainer>
      <S.InputGroup>
        <S.Label htmlFor="nickname">닉네임</S.Label>
        <S.Input
          id="nickname"
          type="text"
          placeholder="닉네임을 입력해주세요"
          value={signupData.nickname}
          onChange={e => changeSignupData('nickname', e.target.value)}
        />
        {error.nicknameError && <S.ErrorMessage>{error.nicknameError}</S.ErrorMessage>}
      </S.InputGroup>
    </S.StepContainer>
  );
};
