import { useSignupContext } from '@/features/auth/context/useUserContext';
import * as S from './SignupStep.styles';

interface signupData {
  username: string;
  password: string;
  rePassword: string;
}

export const SignupStep1 = () => {
  const { signupData, changeSignupData, error } = useSignupContext();

  console.log('asdf', signupData);

  return (
    <S.StepContainer>
      <S.InputGroup>
        <S.Label htmlFor="username">아이디</S.Label>
        <S.Input
          id="email"
          type="email"
          placeholder="아이디를 입력해주세요"
          value={signupData.email}
          onChange={e => changeSignupData('email', e.target.value)}
        />
        {error.emailError && <S.ErrorMessage>{error.emailError}</S.ErrorMessage>}
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="password">비밀번호</S.Label>
        <S.Input
          id="password"
          type="password"
          placeholder="비밀번호를 입력해주세요"
          value={signupData.password}
          onChange={e => changeSignupData('password', e.target.value)}
        />
        {error.passwordError && <S.ErrorMessage>{error.passwordError}</S.ErrorMessage>}
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="rePassword">비밀번호 확인</S.Label>
        <S.Input
          id="rePassword"
          type="password"
          placeholder="비밀번호를 다시 입력해주세요"
          value={signupData.rePassword}
          onChange={e => changeSignupData('rePassword', e.target.value)}
        />
        {error.rePasswordError && <S.ErrorMessage>{error.rePasswordError}</S.ErrorMessage>}
      </S.InputGroup>
    </S.StepContainer>
  );
};
