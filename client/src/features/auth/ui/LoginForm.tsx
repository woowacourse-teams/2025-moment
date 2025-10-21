import { useLoginForm } from '@/features/auth/hooks/useLoginForm';
import { Input } from '@/shared/ui/input/Input';
import { useNavigate } from 'react-router';
import { GoogleLoginButton } from './GoogleLoginButton';
import * as S from './LoginForm.styles';

export const LoginForm = () => {
  const navigate = useNavigate();
  const { formData, errors, handleChange, handleSubmit, isDisabled } = useLoginForm();

  const handleSignupClick = () => {
    navigate('/signup');
  };

  const handleNewPassword = () => {
    navigate('/find-password');
  };

  return (
    <S.LoginFormWrapper>
      <S.LoginFormContainer onSubmit={handleSubmit}>
        <S.LoginFormTitleContainer>
          <S.LoginLogoTitleContainer>
            <S.LogoImage src="/images/logo.webp" alt="" />
            <S.LogoTitle>Moment</S.LogoTitle>
          </S.LoginLogoTitleContainer>
          <S.LoginTitle>로그인</S.LoginTitle>
          <S.LoginDescription>이메일과 비밀번호를 입력하여 로그인해주세요.</S.LoginDescription>
        </S.LoginFormTitleContainer>
        <S.LoginFormContent>
          <S.InputGroup>
            <S.Label htmlFor="email">이메일</S.Label>
            <Input
              id="email"
              type="email"
              placeholder="이메일을 입력해주세요"
              value={formData.email}
              onChange={handleChange('email')}
            />
            <S.ErrorMessage>{errors.email || ' '}</S.ErrorMessage>
          </S.InputGroup>
          <S.InputGroup>
            <S.Label htmlFor="password">비밀번호</S.Label>
            <Input
              id="password"
              type="password"
              placeholder="비밀번호를 입력해주세요"
              value={formData.password}
              onChange={handleChange('password')}
            />
            <S.ErrorMessage>{errors.password || ' '}</S.ErrorMessage>
          </S.InputGroup>
        </S.LoginFormContent>
        <S.LoginButton type="submit" disabled={isDisabled}>
          로그인
        </S.LoginButton>
      </S.LoginFormContainer>
      <S.LoginFooter>
        <GoogleLoginButton />
        <S.LoginFooterContent>
          <S.LoginSignupContainer>
            <S.NewPasswordLink type="button" onClick={handleNewPassword}>
              비밀번호 찾기
            </S.NewPasswordLink>
          </S.LoginSignupContainer>
          <S.LoginSignupContainer>
            <p>아직 회원이 아니신가요?</p>
            <S.LoginSignupLink type="button" onClick={handleSignupClick}>
              회원가입
            </S.LoginSignupLink>
          </S.LoginSignupContainer>
        </S.LoginFooterContent>
      </S.LoginFooter>
    </S.LoginFormWrapper>
  );
};
