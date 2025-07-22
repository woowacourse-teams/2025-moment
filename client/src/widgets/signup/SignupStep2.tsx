import { useUserContext } from '@/features/auth/context/useUserContext';
import { Input } from '@/shared/ui/input/Input';
import * as S from './SignupStep.styles';

export const SignupStep2 = () => {
  const { userData, changeUserData, error } = useUserContext();

  return (
    <S.StepContainer>
      <S.InputGroup>
        <S.Label htmlFor="nickname">닉네임</S.Label>
        <Input
          id="nickname"
          type="text"
          placeholder="닉네임을 입력해주세요"
          value={userData.nickname}
          onChange={e => changeUserData('nickname', e.target.value)}
        />
        {error.nicknameError && <S.ErrorMessage>{error.nicknameError}</S.ErrorMessage>}
      </S.InputGroup>
    </S.StepContainer>
  );
};
