import * as S from './GoogleLoginButton.styles';

const googleLoginUrl = process.env.REACT_APP_GOOGLE_LOGIN_REDIRECTION_URL || '';

const handleGoogleLogin = () => {
  window.location.href = googleLoginUrl;
};

export const GoogleLoginButton = () => {
  return (
    <S.GoogleLoginButton onClick={handleGoogleLogin}>
      <S.GoogleLoginButtonIcon src="/google.png" alt="google" />
      Sign in with Google
    </S.GoogleLoginButton>
  );
};
