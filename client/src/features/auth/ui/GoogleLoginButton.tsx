import { useEffect, useState } from 'react';
import * as S from './GoogleLoginButton.styles';
import { useGoogleLoginMutation } from '../api/useGoogleLoginMutation';

const googleLoginUrl = process.env.REACT_APP_GOOGLE_LOGIN_REDIRECTION_URL || '';

export const GoogleLoginButton = () => {
  const [isWebView, setIsWebView] = useState(false);

  const { mutate: googleLogin } = useGoogleLoginMutation();

  useEffect(() => {
    if (typeof window !== 'undefined' && window.ReactNativeWebView) {
      setIsWebView(true);
    }
  }, []);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      window.onGoogleLoginSuccess = (token: string) => {
        googleLogin(token);
      };
    }
  }, [googleLogin]);

  const handleGoogleLogin = () => {
    if (isWebView && window.ReactNativeWebView) {
      window.ReactNativeWebView.postMessage(
        JSON.stringify({ type: 'AUTH_REQUEST', provider: 'google' }),
      );
    } else {
      window.location.href = googleLoginUrl;
    }
  };

  return (
    <S.GoogleLoginButton type="button" onClick={handleGoogleLogin} aria-label="Google 로그인">
      <S.GoogleLoginButtonIcon src="/images/google.webp" alt="" />
      Sign in with Google
    </S.GoogleLoginButton>
  );
};
