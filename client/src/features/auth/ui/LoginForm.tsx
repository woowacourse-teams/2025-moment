import { useLoginForm } from '@/features/auth/hooks/useLoginForm';
import { useNavigate } from 'react-router';
import * as S from './LoginForm.styles';

export const LoginForm = () => {
  const navigate = useNavigate();
  const { formData, error, isLoading, handleInputChange, handleSubmit, isDisabled } =
    useLoginForm();

  const handleLoginClick = () => {
    // TODO: 로그인 로직 구현
  };

  const handleSignupClick = () => {
    navigate('/signup');
  };

  return (
    <S.LoginFormWrapper onSubmit={handleSubmit}>
      <S.LoginFormTitleContainer>
        <S.LoginLogoTitleContainer>
          <S.LogoImage src="/logo.webp" alt="Moment Logo Image" />
          <S.LogoTitle>모멘트</S.LogoTitle>
        </S.LoginLogoTitleContainer>
        <S.LoginTitle>로그인</S.LoginTitle>
        <S.LoginDescription>이메일과 비밀번호를 입력하여 로그인해주세요.</S.LoginDescription>
      </S.LoginFormTitleContainer>
      <S.LoginFormContent>
        <S.InputGroup>
          <S.Label htmlFor="email">이메일</S.Label>
          <S.Input
            id="email"
            type="email"
            placeholder="이메일을 입력해주세요"
            value={formData.email}
            onChange={handleInputChange('email')}
          />
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
        </S.InputGroup>
      </S.LoginFormContent>
      <S.LoginFooter>
        <S.LoginButton type="submit" disabled={isDisabled}>
          로그인
        </S.LoginButton>
        <S.LoginFooterContent>
          <S.LoginForgotPassword>비밀번호를 잊으셨나요?</S.LoginForgotPassword>
          <S.LoginSignupContainer>
            <span>아직 회원이 아니신가요?</span>
            <S.LoginSignupLink onClick={handleSignupClick}>회원가입</S.LoginSignupLink>
          </S.LoginSignupContainer>
        </S.LoginFooterContent>
      </S.LoginFooter>
    </S.LoginFormWrapper>
  );
};
