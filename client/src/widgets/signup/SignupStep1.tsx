import { useUserContext } from '@/features/auth/context/useUserContext';
import * as S from './SignupStep.styles';

interface SignupStep1Props {
  password: {
    password: string;
    rePassword: string;
  };
  setPassword: (password: { password: string; rePassword: string }) => void;
}

export const SignupStep1 = ({ password, setPassword }: SignupStep1Props) => {
  const { userData, changeUserData, error } = useUserContext();

  return (
    <S.StepContainer>
      <S.InputGroup>
        <S.Label htmlFor="username">아이디</S.Label>
        <S.Input
          id="email"
          type="email"
          placeholder="아이디를 입력해주세요"
          value={userData.email}
          onChange={e => changeUserData('email', e.target.value)}
        />
        {error.emailError && <S.ErrorMessage>{error.emailError}</S.ErrorMessage>}
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="password">비밀번호</S.Label>
        <S.Input
          id="password"
          type="password"
          placeholder="비밀번호를 입력해주세요"
          value={password.password}
          onChange={e => setPassword({ ...password, password: e.target.value })}
        />
        {/* Todo: 비밀번호 오류 처리는 추후 유효성 검사 로직 포함하여 처리 */}
        {/* {error.passwordError && <S.ErrorMessage>{error.passwordError}</S.ErrorMessage>} */}
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="rePassword">비밀번호 확인</S.Label>
        <S.Input
          id="rePassword"
          type="password"
          placeholder="비밀번호를 다시 입력해주세요"
          value={password.rePassword}
          onChange={e => setPassword({ ...password, rePassword: e.target.value })}
        />
        {/* Todo: 비밀번호 오류 처리는 추후 유효성 검사 로직 포함하여 처리 */}
        {/* {error.rePasswordError && <S.ErrorMessage>{error.rePasswordError}</S.ErrorMessage>} */}
      </S.InputGroup>
    </S.StepContainer>
  );
};
