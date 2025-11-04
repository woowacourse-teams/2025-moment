import * as S from './GoogleLoginButton.styles';

const googleLoginUrl = process.env.REACT_APP_GOOGLE_LOGIN_REDIRECTION_URL || '';

const handleGoogleLogin = () => {
  window.location.href = googleLoginUrl;
};

export const GoogleLoginButton = () => {
  return (
    <S.GoogleLoginButton type="button" onClick={handleGoogleLogin} aria-label="Google 로그인">
      <S.GoogleLoginButtonIcon src="/images/google.webp" alt="" />
      Sign in with Google
    </S.GoogleLoginButton>
  );
};
